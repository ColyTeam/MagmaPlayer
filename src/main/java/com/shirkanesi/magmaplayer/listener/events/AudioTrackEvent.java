package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class AudioTrackEvent {

    @Getter
    @NotNull
    protected AudioTrack audioTrack;

    public AudioTrackEvent(@NotNull AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    public AudioPlayer getAudioPlayer() {
        return audioTrack.getAudioPlayer();
    }
}
