package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.model.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class AudioTrackPauseEvent extends AudioTrackEvent {

    private final boolean paused;

    public AudioTrackPauseEvent(@NotNull AudioTrack audioTrack, boolean paused) {
        super(audioTrack);
        this.paused = paused;
    }
}
