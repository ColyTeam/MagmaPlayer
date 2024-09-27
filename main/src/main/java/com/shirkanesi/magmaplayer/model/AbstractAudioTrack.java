package com.shirkanesi.magmaplayer.model;

import com.shirkanesi.magmaplayer.listener.AudioTrackObserver;
import lombok.Getter;

@Getter
public abstract class AbstractAudioTrack extends UserData implements AudioTrack {

    private final AudioTrackObserver audioTrackObserver;

    public AbstractAudioTrack() {
        this.audioTrackObserver = new AudioTrackObserver(this);
    }
}
