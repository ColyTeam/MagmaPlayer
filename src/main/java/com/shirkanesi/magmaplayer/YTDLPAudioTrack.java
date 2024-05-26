package com.shirkanesi.magmaplayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirkanesi.magmaplayer.exception.AudioPlayerException;
import com.shirkanesi.magmaplayer.exception.AudioTrackPullException;
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
public class YTDLPAudioTrack extends AbstractAudioTrack {

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
    private boolean pullStarted = false;

    private Thread pullThread;
    private File tempFile;

    private FileOutputStream fileOutputStream;

    public YTDLPAudioTrack(String url) {
        this.url = url;
    }

    @Override
    public void load() {
        new Thread(() -> {
            try {
                final String streamUrl = this.findStreamUrl(this.url);
                log.debug("Stream-URL: {}", streamUrl);

                tempFile = File.createTempFile("audio-buffer", ".opus");
                tempFile.deleteOnExit();

                this.pullAudioStreamAsync(streamUrl, tempFile);
                this.startStreamingFrom(tempFile);
            } catch (InterruptedException | IOException e) {
                //TODO
            }
        }).start();

        // Ensure space gets cleaned on program termination.
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    private void startStreamingFrom(File tempFile) {
        new Thread(() -> {
            try {
                // TODO: fixme
                TimeUnit.SECONDS.sleep(1);  // necessary because we need to wait for the first frames to arrive.
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
                        // TODO: find better solution
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
    public synchronized void jumpTo(int seconds) {
        try {
            if ((long) seconds * SAMPLE_RATE < this.nextAudioPacket.getGranulePosition()) {
                this.restart();
            }
            this.opusFile.skipToGranule((long) seconds * SAMPLE_RATE);
        } catch (IOException e) {
            throw new AudioPlayerException(e);
        }
    }

    @Override
    @SneakyThrows
    public void restart() {
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
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
            }
            log.debug("Audio-track has been pulled after {} attempt(s)", failedAttempts + 1);

            this.ready = true;
            this.finished = false;
            getAudioTrackObserver().triggerAudioTrackStarted();
        } catch (IOException e) {
            throw new AudioPlayerException(e);
        }

    }

    @Override
    public ByteBuffer nextSnippet() {
        return ByteBuffer.wrap(nextAudioPacket.getData());
    }

    @Override
    public void close() {
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

    @SneakyThrows // FIXME
    public AudioTrackInformation getInformation() {
        if (trackInformation != null) {
            // we did already load the information before
            return trackInformation;
        }

        final String findInformation = String.format(FIND_INFORMATION_COMMAND, this.url);
        Process process = Runtime.getRuntime().exec(findInformation);
        try {
            // Timeout seems to be necessary for some reason.
            process.waitFor(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            process.destroy();
        }

        String json;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            // yt-dlp will not put any line-breaks into the response ==> one line is enough
            json = bufferedReader.readLine();
        }

        trackInformation = new ObjectMapper().readValue(json, YTDLPAudioTrackInformation.class);
        return trackInformation;
    }
}
