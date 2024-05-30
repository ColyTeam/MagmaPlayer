package com.shirkanesi.magmaplayer.ytdlp;

import com.shirkanesi.magmaplayer.AbstractAudioPlaylist;
import com.shirkanesi.magmaplayer.exception.AudioTrackPullException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class YTDLPAudioPlaylist extends AbstractAudioPlaylist implements YTDLPAudioItem {

    private static final String LOAD_PLAYLIST_COMMAND = "yt-dlp --flat-playlist --print \"%(url)s\"";

    private final String url;

    public YTDLPAudioPlaylist(String url) {
        this.url = url;
    }

    @Override
    public void load() {
        try {
            List<String> urls = getPlaylistTracks();

            for (String url : urls) {
                tracks.add(new YTDLPAudioTrack(url));
            }
        } catch (IOException e) {
            throw new AudioTrackPullException("Failed while loading playlist " + url, e);
        }
    }

    private List<String> getPlaylistTracks() throws IOException {
        Process process = Runtime.getRuntime().exec(LOAD_PLAYLIST_COMMAND + " " + url);
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
                }
            }
        }
        return urls;
    }

}
