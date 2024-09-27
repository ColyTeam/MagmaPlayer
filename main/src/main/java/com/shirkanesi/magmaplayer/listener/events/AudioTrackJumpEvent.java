package com.shirkanesi.magmaplayer.listener.events;

import com.shirkanesi.magmaplayer.model.AudioTrack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class AudioTrackJumpEvent extends AudioTrackEvent {

    // times in seconds
    private final long fromTime;
    private final long toTime;

    public AudioTrackJumpEvent(@NotNull AudioTrack audioTrack, long fromTime, long toTime) {
        super(audioTrack);
        this.fromTime = fromTime;
        this.toTime = toTime;
    }
}
