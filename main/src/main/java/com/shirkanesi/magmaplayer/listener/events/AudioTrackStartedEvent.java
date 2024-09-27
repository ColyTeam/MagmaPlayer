package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.model.AudioTrack;
import org.jetbrains.annotations.NotNull;

public class AudioTrackStartedEvent extends AudioTrackEvent{
    public AudioTrackStartedEvent(@NotNull AudioTrack audioTrack) {
        super(audioTrack);
    }
}
