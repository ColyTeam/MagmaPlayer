package com.shirkanesi.magmaplayer.exception;

public class AudioTrackPullException extends AudioPlayerException {

    public AudioTrackPullException() {
    }

    public AudioTrackPullException(String message) {
        super(message);
    }

    public AudioTrackPullException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioTrackPullException(Throwable cause) {
        super(cause);
    }

    public AudioTrackPullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
