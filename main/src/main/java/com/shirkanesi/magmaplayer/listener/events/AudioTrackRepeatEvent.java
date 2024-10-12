package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.model.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class AudioTrackRepeatEvent extends AudioTrackEvent {

    private final boolean repeating;

    public AudioTrackRepeatEvent(@NotNull AudioTrack audioTrack, boolean repeating) {
        super(audioTrack);
        this.repeating = repeating;
    }
}
