package com.shirkanesi.magmaplayer.listener;

import com.shirkanesi.magmaplayer.listener.events.AudioTrackEndEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackPauseEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackSkippedEvent;
import com.shirkanesi.magmaplayer.listener.events.AudioTrackStartedEvent;

public interface AudioTrackEventListener {

    default void onAudioTrackStarted(final AudioTrackStartedEvent event) {}

    default void onAudioTrackEnded(final AudioTrackEndEvent event) {}

    default void onAudioTrackSkipped(final AudioTrackSkippedEvent event) {}

    default void onAudioTrackPaused(final AudioTrackPauseEvent event) {}

}