package com.shirkanesi.magmaplayer;

import java.io.Closeable;
import java.nio.ByteBuffer;

public interface AudioTrack extends Closeable {

    void load();

    void jumpTo(int seconds);

    void restart();

    boolean isFinished();

    boolean canProvide();

    ByteBuffer nextSnippet();

    void close();

}
