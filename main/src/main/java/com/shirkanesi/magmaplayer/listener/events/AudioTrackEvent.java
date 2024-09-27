package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.model.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class AudioTrackEvent {

    @NotNull
    protected AudioTrack audioTrack;

    public AudioTrackEvent(@NotNull AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    public AudioPlayer getAudioPlayer() {
        return audioTrack.getAudioPlayer();
    }
}
