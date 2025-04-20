package com.shirkanesi.magmaplayer.ytdlp;

import com.shirkanesi.magmaplayer.model.AbstractAudioPlaylist;
import com.shirkanesi.magmaplayer.exception.AudioTrackPullException;
import com.shirkanesi.magmaplayer.util.FormatUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class YTDLPAudioPlaylist extends AbstractAudioPlaylist implements YTDLPAudioItem {

    private static final String[] LOAD_PLAYLIST_COMMAND = {"yt-dlp", "--no-download", "--flat-playlist", "--print",
            "\"NAME:%%(playlist_title)s\"", "--print", "\"%%(url)s\"", "\"%s\""};

    private final String url;
    private String cookiesFilePath;

    public YTDLPAudioPlaylist(String url) {
        this.url = url;
    }

    public YTDLPAudioPlaylist(String url, Object userData) {
        this(url);
        setUserData(userData);
    }

    public YTDLPAudioPlaylist(String url, Object userData, String cookiesFilePath) {
        this(url);
        setUserData(userData);
        this.cookiesFilePath = cookiesFilePath;
    }

    @Override
    public void load() {
        try {
            List<String> urls = getPlaylistTracksAndSetName();

            for (String url : urls) {
                tracks.add(new YTDLPAudioTrack(url, getUserData(), cookiesFilePath));
            }
        } catch (IOException e) {
            throw new AudioTrackPullException("Failed while loading playlist " + url, e);
        }
    }

    private List<String> getPlaylistTracksAndSetName() throws IOException {
        final String[] args = FormatUtils.format(LOAD_PLAYLIST_COMMAND, this.url);
        Process process = Runtime.getRuntime().exec(args);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
        }

        List<String> urls = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("http")) {
                    urls.add(line);
                } else if (line.startsWith("NAME:")) {
                    this.name = line.split("NAME:", 2)[1];
                }
            }
        }
        return urls;
    }

}
