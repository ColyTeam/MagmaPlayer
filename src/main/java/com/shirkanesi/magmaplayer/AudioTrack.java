package com.shirkanesi.magmaplayer;

import com.shirkanesi.magmaplayer.listener.AudioTrackObserver;

import java.io.Closeable;
import java.nio.ByteBuffer;

public interface AudioTrack extends Closeable {

    void jumpTo(int seconds);

    void restart();

    boolean isFinished();

    boolean canProvide();

    ByteBuffer nextSnippet();

    void close();

    AudioPlayer getAudioPlayer();

    AudioTrackObserver getAudioTrackObserver();

    AudioTrackInformation getInformation();

}
