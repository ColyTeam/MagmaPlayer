package com.shirkanesi.magmaplayer;

import com.shirkanesi.magmaplayer.discord.MagmaPlayerSendHandler;
import com.shirkanesi.magmaplayer.listener.AudioTrackEventListener;
import com.shirkanesi.magmaplayer.listener.FiresEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackEndEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackPauseEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackRepeatEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackSkippedEvent;
import com.shirkanesi.magmaplayer.model.AudioPlaylist;
import com.shirkanesi.magmaplayer.model.AudioTrack;
import com.shirkanesi.magmaplayer.model.Pauseable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class AudioPlayer implements Pauseable, Closeable, AudioTrackEventListener {

    private AudioTrack audioTrack;

    private final BlockingQueue<AudioTrack> trackQueue = new LinkedBlockingQueue<>();

    private boolean paused = false;
    @Getter
    private boolean repeating = false;

    private AudioSendHandler sendHandler;

    public AudioPlayer() {
        this((AudioTrack) null);
    }

    public AudioPlayer(VoiceChannel voiceChannel) {
        voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
        voiceChannel.getGuild().getAudioManager().setSendingHandler(this.createSendHandler());
    }

    public AudioPlayer(AudioTrack audioTrack) {
        this.setTrack(audioTrack);

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

    }

    @Override
    @FiresEvent(value = AudioTrackPauseEvent.class, onEveryPass = false)
    public void pause() {
        pauseImpl(true);
    }

    @Override
    @FiresEvent(value = AudioTrackPauseEvent.class, onEveryPass = false)
    public void resume() {
        pauseImpl(false);
    }

    private void pauseImpl(boolean state) {
        this.paused = state;
        if (this.audioTrack != null) {
            this.audioTrack.getAudioTrackObserver().triggerAudioTrackPaused(state);
        }
    }

    @Override
    public boolean isPaused() {
        return paused || !this.canProvide();
    }

    private synchronized void enqueueTrack(AudioTrack audioTrack) {
        this.trackQueue.add(audioTrack);
        log.debug("Enqueue track {}", audioTrack);
        audioTrack.getAudioTrackObserver().addEventListener(this);
        if (this.audioTrack == null) {
            this.next();
        }
    }

    public synchronized void enqueue(AudioTrack audioTrack) {
        this.enqueueTrack(audioTrack);
    }

    public synchronized void enqueue(AudioPlaylist audioPlaylist) {
        if (audioPlaylist.getTracks().isEmpty()) {
            audioPlaylist.load();
        }

        for (AudioTrack audioTrack : audioPlaylist.getTracks()) {
            this.enqueueTrack(audioTrack);
        }
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

    @FiresEvent(value = AudioTrackSkippedEvent.class, onEveryPass = false)
    public void next() {
        try {
            if (this.audioTrack != null) {
                if (!this.audioTrack.isFinished()) {
                    this.audioTrack.getAudioTrackObserver().triggerAudioTrackSkipped();
                }
            }
            if (!this.trackQueue.isEmpty()) {
                this.setTrack(this.trackQueue.take());
            } else {
                // Queue ended
                this.audioTrack = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setTrack(AudioTrack audioTrack) {
        if (this.audioTrack != null) {
            this.audioTrack.close();
        }
        if (audioTrack == null) {
            return;
            // why should this call next? The queue has ended
            // Handle playback of next track async for faster return to callee
            //new Thread(this::next).start();
        }
        this.audioTrack = audioTrack;
        this.audioTrack.load();
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

    public List<AudioTrack> getTrackQueue() {
        return new LinkedList<>(this.trackQueue);
    }

    public void clearQueue() {
        this.trackQueue.clear();
    }

    @FiresEvent(value = AudioTrackRepeatEvent.class, onEveryPass = true)
    public void toggleRepeating() {
        this.repeating = !this.repeating;
        if (this.audioTrack != null) {
            this.audioTrack.getAudioTrackObserver().triggerAudioTrackRepeat(repeating);
        }
    }

    @Override
    public void onAudioTrackEnded(AudioTrackEndEvent event) {
        if (repeating) {
            setTrack(this.audioTrack);
        } else {
            next();
        }
    }
}
