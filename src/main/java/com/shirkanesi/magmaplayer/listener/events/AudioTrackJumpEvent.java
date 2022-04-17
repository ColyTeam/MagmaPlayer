package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class AudioTrackJumpEvent extends AudioTrackEvent {

    // times in seconds
    @Getter
    private final long fromTime;
    @Getter
    private final long toTime;

    public AudioTrackJumpEvent(@NotNull AudioTrack audioTrack, long fromTime, long toTime) {
        super(audioTrack);
        this.fromTime = fromTime;
        this.toTime = toTime;
    }
}
