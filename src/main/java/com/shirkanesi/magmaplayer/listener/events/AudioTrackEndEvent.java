package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.AudioTrack;
import org.jetbrains.annotations.NotNull;

public class AudioTrackEndEvent extends AudioTrackEvent{
    public AudioTrackEndEvent(@NotNull AudioTrack audioTrack) {
        super(audioTrack);
    }
}
