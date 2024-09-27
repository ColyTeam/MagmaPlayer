package com.shirkanesi.magmaplayer.model;

import java.util.List;

public interface AudioPlaylist extends AudioItem {

    List<AudioTrack> getTracks();

}
