package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.AudioPlayer;
import com.shirkanesi.magmaplayer.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class AudioTrackEvent {

    @Getter
    @NotNull
    protected AudioTrack audioTrack;

    @Getter
    @NotNull
    protected AudioPlayer audioPlayer;


}
