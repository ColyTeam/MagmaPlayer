package com.shirkanesi.magmaplayer;

import com.shirkanesi.magmaplayer.exception.AudioPlayerException;
import com.shirkanesi.magmaplayer.exception.AudioTrackPullException;
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
public class YTDLPAudioTrack extends AbstractAudioTrack {

    // Note: yt-dlp -j <URL> will give information about the video (e.g. duration...)

    // Constants
    private static final int SAMPLE_RATE = 48000;
    private static final String FIND_STREAM_COMMAND = "yt-dlp -g -f bestaudio -S \"+size,+br,+res,+fps\" \"%s\"";
    private static final String FALLBACK_FIND_STREAM_COMMAND = "yt-dlp -g -S \"+size,+br,+res,+fps\" \"%s\"";
    private static final String PULL_STREAM_COMMAND = "ffmpeg -loglevel quiet -hide_banner -i \"%s\" -y -vbr 0 -ab 128k -ar 48k -f opus -";

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

    private Runnable onAfterFinish;

    private File tempFile;

    private FileOutputStream fileOutputStream;

    private final Semaphore readySemaphore = new Semaphore(0);

    @SneakyThrows   // TODO
    public YTDLPAudioTrack(String url) {
        this.url = url;

        // Ensure space gets cleaned on program termination.
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
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
    }

    /**
     * Sets a callback-function called, when the track is finished (or skipped)
     * @param onAfterFinish the callback to be called
     */
    public void setOnAfterFinish(Runnable onAfterFinish) {
        this.onAfterFinish = onAfterFinish;
    }

    private void startStreamingFrom(File tempFile) {
        new Thread(() -> {
            try {
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
                    this.readySemaphore.release();
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
            log.info("Initial stream-search not successfully. Trying without \"-f bestaudio\"!");
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
                new Thread(this.onAfterFinish).start();
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
            this.readySemaphore.acquire();

            // This looks like busy-waiting. Well that's right. However, this will wait at most 5 seconds.
            // Only waiting until the first opus-frame is available.
            int failedAttempts = 0;
            while (this.opusFile == null) {
                FileInputStream input = new FileInputStream(tempFile);
                try {
                    this.opusFile = new OpusFile(new OggFile(input));
                } catch (IllegalArgumentException e2) {
                    if (failedAttempts++ > 5) {
                        throw new AudioTrackPullException("Could not pull first frame of audio-track in 5 seconds.");
                    }
                    input.close();
                    TimeUnit.SECONDS.sleep(1);
                }
            }
            log.debug("Audio-track has been pulled within {} seconds", failedAttempts);

            this.ready = true;
            this.finished = false;
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
                // this.pullThread.stop(); // interrupted should be preferred.
                this.pullThread.interrupt();
            }
            if (this.opusFile != null) {
                this.opusFile.close();
            }
            if (this.fileOutputStream != null) {
                this.fileOutputStream.close();
            }
        } catch (IOException e) {
            throw new AudioPlayerException(e);
        }
    }

}
