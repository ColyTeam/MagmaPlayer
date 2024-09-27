package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.model.AudioTrack;
import org.jetbrains.annotations.NotNull;

public class AudioTrackSkippedEvent extends AudioTrackEvent{
    public AudioTrackSkippedEvent(@NotNull AudioTrack audioTrack) {
        super(audioTrack);
    }
}
