package com.shirkanesi.magmaplayer;

import java.util.List;

public interface AudioPlaylist extends AudioItem {

    List<AudioTrack> getTracks();

}
