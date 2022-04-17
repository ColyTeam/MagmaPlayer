package com.shirkanesi.magmaplayer.ytdlp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shirkanesi.magmaplayer.AudioTrackInformation;
import lombok.Getter;

// The json will always contain lots of values we don't care about ==> ignore them.
@JsonIgnoreProperties(ignoreUnknown = true)
public class YTDLPAudioTrackInformation extends AudioTrackInformation {

    @Getter
    @JsonProperty("title")
    private String title;

    @Getter
    @JsonProperty("description")
    private String description;

    @Getter
    @JsonProperty("uploader")
    private String creator;

    @Getter
    @JsonProperty("webpage_url")
    private String link;

}
