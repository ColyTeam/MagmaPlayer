package com.shirkanesi.magmaplayer.ytdlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirkanesi.magmaplayer.AbstractAudioTrack;
import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrackInformation;
import com.shirkanesi.magmaplayer.exception.AudioPlayerException;
import com.shirkanesi.magmaplayer.exception.AudioTrackPullException;
import com.shirkanesi.magmaplayer.listener.FiresEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackJumpEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackStartedEvent;
import lombok.Setter;
import com.shirkanesi.magmaplayer.ytdlp.model.YTDLPAudioTrackInformation;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@Slf4j
public class YTDLPAudioTrack extends AbstractAudioTrack implements YTDLPAudioItem {

    // Note: yt-dlp -j <URL> will give information about the video (e.g. duration...)

    // Constants
    private static final int SAMPLE_RATE = 48000;
    private static final String FIND_STREAM_COMMAND = "yt-dlp -g -f bestaudio -S \"+size,+br,+res,+fps\" \"%s\"";
    private static final String FALLBACK_FIND_STREAM_COMMAND = "yt-dlp -g -S \"+size,+br,+res,+fps\" \"%s\"";
    private static final String FIND_INFORMATION_COMMAND = "yt-dlp -J \"%s\"";
    private static final String PULL_STREAM_COMMAND = "ffmpeg -loglevel quiet -hide_banner -i \"%s\" -y -vbr 0 -ab 128k -ar 48k -f opus -";
    private static final int MAX_ATTEMPTS = 10;
    private static final int MIN_AVAILABLE_BYTES = 512;

    @Getter
    @Setter
    private AudioPlayer audioPlayer;

    private YTDLPAudioTrackInformation trackInformation;

    /**
     * The video's URL
     */
    private final String url;

    private OpusFile opusFile;
    private OpusAudioData nextAudioPacket;

    private boolean finished = false;
    private boolean ready = false;

    private Thread pullThread;
    private File tempFile;

    private FileOutputStream fileOutputStream;

    public YTDLPAudioTrack(String url) {
        this.url = url;
    }

    @FiresEvent(value = AudioTrackStartedEvent.class, onEveryPass = true)
    @Override
    public void load() {
        new Thread(() -> {
            try {
                final String streamUrl = this.findStreamUrl(this.url);
                log.debug("Stream-URL: {}", streamUrl);

                tempFile = File.createTempFile("audio-buffer", ".opus");
                tempFile.deleteOnExit();

                this.pullAudioStreamAsync(streamUrl, tempFile);
                this.startStreamingFrom();
            } catch (InterruptedException | IOException e) {
                throw new AudioTrackPullException(e);
            }
        }).start();

        // Ensure space gets cleaned on program termination.
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    private void startStreamingFrom() {
        new Thread(() -> {
            try {
                this.restart();
            } catch (Exception exception) {
                log.warn("Exception while reading from file!", exception);
            }
        }).start();
    }

    private void pullAudioStreamAsync(String streamUrl, File tempFile) {
        this.pullThread = new Thread(() -> {
            Process pullProcess = null;
            try {
                String pullCommand = String.format(PULL_STREAM_COMMAND, streamUrl);

                pullProcess = Runtime.getRuntime().exec(pullCommand);

                try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                    this.fileOutputStream = fileOutputStream;

                    while (pullProcess.getInputStream().available() < MIN_AVAILABLE_BYTES) {
                        // Yes, this is busy waiting. The author does not know a better solution.
                        Thread.sleep(100);
                    }

                    long dataLength = pullProcess.getInputStream().transferTo(fileOutputStream);
                    log.debug(String.format("Pulled %.2fMiB to %s", dataLength / 1048576.0, tempFile.getName()));
                }

                // Await termination of the pulling process. Required to actually kill the pull-process iff necessary.
                pullProcess.waitFor();
            } catch (IOException e) {
                log.warn("Error while pulling from source-stream", e);
            } catch (InterruptedException e) {
                pullProcess.destroy();
            }
        });
        this.pullThread.start();
    }

    private String findStreamUrl(String url) throws IOException, InterruptedException {
        String streamUrl = this.findStreamUrl(FIND_STREAM_COMMAND, url);
        if (streamUrl == null) {
            log.info("Initial stream-search not successfully. Trying with the fallback-command!");
            streamUrl = this.findStreamUrl(FALLBACK_FIND_STREAM_COMMAND, url);
        }
        return streamUrl;
    }

    private String findStreamUrl(String commandTemplate, String url) throws IOException {
        final String findStreamUrlCommand = String.format(commandTemplate, url);
        Process process = Runtime.getRuntime().exec(findStreamUrlCommand);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
        }

        handleErrorInProcess(process);

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return bufferedReader.readLine();
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean canProvide() {
        if (!this.ready || finished) {
            return false;
        }
        try {
            nextAudioPacket = opusFile.getNextAudioPacket();
            boolean canProvide = nextAudioPacket != null;
            if (!canProvide) {
                finished = true;
                getAudioTrackObserver().triggerAudioTrackEnded();
                this.close();
            }
            return canProvide;
        } catch (IOException e) {
            this.ready = false; // TODO: check!
            return false;
        }
    }

    @Override
    @FiresEvent(value = AudioTrackJumpEvent.class, onEveryPass = true)
    public synchronized void jumpTo(int seconds) throws AudioPlayerException {
        try {
            long currentPosition = this.nextAudioPacket.getGranulePosition();
            if ((long) seconds * SAMPLE_RATE < currentPosition) {
                this.restart();
            }
            this.opusFile.skipToGranule((long) seconds * SAMPLE_RATE);
            getAudioTrackObserver().triggerAudioTrackJump(currentPosition / SAMPLE_RATE, this.nextAudioPacket.getGranulePosition() / SAMPLE_RATE);
        } catch (IOException e) {
            throw new AudioPlayerException("Could not jump to time", e);
        }
    }

    @Override
    @SneakyThrows
    @FiresEvent(AudioTrackStartedEvent.class)
    public void restart() throws AudioPlayerException {
        try {
            this.ready = false;
            if (this.opusFile != null) {
                this.opusFile.close();
            }

            // This looks like busy-waiting. Well that's right. However, this will wait at most seconds.
            // Only waiting until the first opus-frame is available.
            int failedAttempts = 0;
            while (this.opusFile == null) {
                FileInputStream input = new FileInputStream(tempFile);
                try {
                    this.opusFile = new OpusFile(new OggFile(input));
                } catch (IllegalArgumentException e2) {
                    if (failedAttempts++ > MAX_ATTEMPTS) {
                        throw new AudioTrackPullException(
                                "Could not pull first frame of audio-track in " + MAX_ATTEMPTS + " seconds.");
                    }
                    input.close();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
            log.debug("Audio-track has been pulled after {} attempt(s)", failedAttempts + 1);

            this.ready = true;
            this.finished = false;
            getAudioTrackObserver().triggerAudioTrackStarted();
        } catch (IOException e) {
            throw new AudioPlayerException("Could not load track", e);
        }
    }

    @Override
    public ByteBuffer nextSnippet() {
        return ByteBuffer.wrap(nextAudioPacket.getData());
    }

    @Override
    public void close() throws AudioPlayerException {
        try {
            if (pullThread != null) {
                this.pullThread.interrupt();
            }
            if (this.opusFile != null) {
                this.opusFile.close();
            }
            if (this.fileOutputStream != null) {
                this.fileOutputStream.close();
            }
            if (this.tempFile != null) {
                this.tempFile.delete();
            }
        } catch (IOException e) {
            throw new AudioPlayerException(e);
        }
    }

    public AudioTrackInformation getInformation() throws AudioPlayerException {
        if (trackInformation != null) {
            // we did already load the information before
            return this.trackInformation;
        }

        try {
            final String findInformation = String.format(FIND_INFORMATION_COMMAND, this.url);
            Process process = Runtime.getRuntime().exec(findInformation);
            try {
                // Timeout seems to be necessary for some reason.
                process.waitFor(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                process.destroy();
            }

            handleErrorInProcess(process);

            String json;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                // yt-dlp will not put any line-breaks into the response ==> one line is enough
                json = bufferedReader.readLine();
            }

            trackInformation = new ObjectMapper().readValue(json, YTDLPAudioTrackInformation.class);
            return trackInformation;
        } catch (IOException e) {
            throw new AudioPlayerException("Could not load track information", e);
        }
    }

    private void handleErrorInProcess(Process process) {
        if (process.exitValue() != 0) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append("\n");
                }

            } catch (IOException e) {
                throw new AudioTrackPullException("Error while reading from source-stream", e);
            }
            throw new AudioTrackPullException("Error while reading from source-stream: " + content);
        }
    }

}
