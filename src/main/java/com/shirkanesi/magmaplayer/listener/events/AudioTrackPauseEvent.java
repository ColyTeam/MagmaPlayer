package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import org.jetbrains.annotations.NotNull;

public class AudioTrackPauseEvent extends AudioTrackEvent {
    public AudioTrackPauseEvent(@NotNull AudioTrack audioTrack) {
        super(audioTrack);
    }
}
