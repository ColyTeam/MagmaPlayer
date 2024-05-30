package com.shirkanesi.magmaplayer;

import com.shirkanesi.magmaplayer.listener.AudioTrackObserver;
import lombok.Getter;

@Getter
public abstract class AbstractAudioTrack implements AudioTrack {

    private final AudioTrackObserver audioTrackObserver;

    public AbstractAudioTrack() {
        this.audioTrackObserver = new AudioTrackObserver(this);
    }
}
