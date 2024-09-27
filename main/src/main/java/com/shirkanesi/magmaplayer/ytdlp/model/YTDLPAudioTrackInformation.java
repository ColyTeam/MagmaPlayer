package com.shirkanesi.magmaplayer.ytdlp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shirkanesi.magmaplayer.model.AudioTrackInformation;
import lombok.Getter;

// The json will always contain lots of values we don't care about ==> ignore them.
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class YTDLPAudioTrackInformation extends AudioTrackInformation {

    @JsonProperty("title")
    private String title;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("description")
    private String description;

    @JsonProperty("uploader")
    private String creator;

    @JsonProperty("webpage_url")
    private String link;

    @JsonProperty("duration")
    private int duration;

}
