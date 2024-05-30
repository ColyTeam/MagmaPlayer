package com.shirkanesi.magmaplayer;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAudioPlaylist implements AudioPlaylist {

    protected List<AudioTrack> tracks = new ArrayList<>();

    @Override
    public List<AudioTrack> getTracks() {
        return tracks;
    }

}
