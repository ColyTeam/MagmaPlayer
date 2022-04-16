package com.shirkanesi.magmaplayer;

import com.shirkanesi.magmaplayer.discord.MagmaPlayerSendHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class AudioPlayer implements Pauseable, Closeable {

    private AudioTrack audioTrack;

    private final BlockingQueue<AudioTrack> trackQueue = new LinkedBlockingQueue<>();

    private Runnable onAfterFinish;

    private boolean paused = false;

    AudioSendHandler sendHandler;

    public AudioPlayer() {
        this((AudioTrack) null);
    }

    public AudioPlayer(VoiceChannel voiceChannel) {
        voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
        voiceChannel.getGuild().getAudioManager().setSendingHandler(this.createSendHandler());
    }

    public AudioPlayer(AudioTrack audioTrack) {
//        this.audioTrack = audioTrack;
        this.setTrack(audioTrack);

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public void resume() {
        this.paused = false;
    }

    @Override
    public boolean isPaused() {
        return paused || !this.canProvide();
    }

    public synchronized void enqueueTrack(AudioTrack audioTrack) {
        this.trackQueue.add(audioTrack);
        if (this.audioTrack == null) {
            this.next();
        }
    }

    public void setOnAfterFinish(Runnable onAfterFinish) {
        this.onAfterFinish = onAfterFinish;
    }

    public boolean isFinished() {
        return this.audioTrack.isFinished();
    }

    public boolean canProvide() {
        // Order is important! this.paused must be before canProvide!
        return this.audioTrack != null && !this.paused && this.audioTrack.canProvide();
    }

    public void jumpTo(int seconds) {
        this.audioTrack.jumpTo(seconds);
    }

    public ByteBuffer nextSnippet() {
        return this.audioTrack.nextSnippet();
    }

    @Override
    public void close() {
        if (this.audioTrack != null) {
            this.audioTrack.close();
        }
    }

    public void next() {
        try {
            if (this.audioTrack != null) {
                if (this.audioTrack.isFinished()) {
                    // AudioTrackEndEvent
                } else {
                    // AudioTrackSkippedEvent
                }
            }
            this.setTrack(this.trackQueue.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setTrack(AudioTrack audioTrack) {
        if (this.audioTrack != null) {
            this.audioTrack.close();
        }
        if (audioTrack != null) {
            ((YTDLPAudioTrack) audioTrack).setOnAfterFinish(this::next); // fixme: cast
        } else {
            // Handle playback of next track async for faster return to callee
            new Thread(this::next).start();
        }
        this.audioTrack = audioTrack;
    }

    public AudioSendHandler createSendHandler() {
        if (this.sendHandler == null) {
            this.sendHandler = new MagmaPlayerSendHandler(this);
        }
        return this.sendHandler;
    }

    public AudioTrack getCurrentAudioTrack() {
        return this.audioTrack;
    }
}
