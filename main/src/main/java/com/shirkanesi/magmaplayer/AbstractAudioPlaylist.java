package com.shirkanesi.magmaplayer;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAudioPlaylist implements AudioPlaylist {

    @Getter
    protected String name;
    protected List<AudioTrack> tracks = new ArrayList<>();

    @Override
    public List<AudioTrack> getTracks() {
        return tracks;
    }

}
