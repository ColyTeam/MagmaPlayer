package com.shirkanesi.magmaplayer;

public abstract class AudioTrackInformation {

    public abstract String getTitle();

    public abstract String getThumbnail();

    public abstract String getDescription();

    public abstract String getCreator();

    public abstract String getLink();

    public abstract int getDuration();

    @Override
    public String toString() {
        // Description is missing on purpose (as it takes up lots of space)
        return String.format("AudioTrackInformation{\ntitle=%s,\ncreator=%s\nlink=%s\n}",
                getTitle(),
                getCreator(),
                getLink()
        );
    }
}
