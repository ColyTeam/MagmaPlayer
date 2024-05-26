package com.shirkanesi.magmaplayer.listener;

import com.shirkanesi.magmaplayer.AudioTrack;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackEndEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackPauseEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackSkippedEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackStartedEvent;

import java.util.HashSet;
import java.util.Set;

public class AudioTrackObserver {

    private final Set<AudioTrackEventListener> listeners = new HashSet<>();

    private final AudioTrack audioTrack;

    public AudioTrackObserver(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    public void triggerAudioTrackStarted() {
        AudioTrackStartedEvent event = new AudioTrackStartedEvent(audioTrack);
        this.listeners.forEach(listener -> listener.onAudioTrackStarted(event));
    }

    public void triggerAudioTrackEnded() {
        AudioTrackEndEvent event = new AudioTrackEndEvent(audioTrack);
        this.listeners.forEach(listener -> listener.onAudioTrackEnded(event));
    }

    public void triggerAudioTrackSkipped() {
        AudioTrackSkippedEvent event = new AudioTrackSkippedEvent(audioTrack);
        this.listeners.forEach(listener -> listener.onAudioTrackSkipped(event));
    }

    public void triggerAudioTrackPaused() {
        AudioTrackPauseEvent event = new AudioTrackPauseEvent(audioTrack);
        this.listeners.forEach(listener -> listener.onAudioTrackPaused(event));
    }

    public void addEventListener(AudioTrackEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(AudioTrackEventListener listener) {
        this.listeners.remove(listener);
    }

}
