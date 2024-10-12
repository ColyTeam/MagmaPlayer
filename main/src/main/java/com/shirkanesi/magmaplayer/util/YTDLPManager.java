package com.shirkanesi.magmaplayer.util;

import com.shirkanesi.magmaplayer.model.UserData;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioItem;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioPlaylist;
import com.shirkanesi.magmaplayer.ytdlp.YTDLPAudioTrack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
public final class YTDLPManager {

    private static final String YT_DLP_LINUX_DOWNLOAD_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_WINDOWS_DOWNLOAD_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";

    /**
     * Get the version of yt-dlp
     *
     * @return an optional containing the version-string of yt-dlp (or empty iff not found)
     */
    public static synchronized Optional<String> getYTDLPVersion() {
        try {
            Process exec = Runtime.getRuntime().exec("yt-dlp --version");
            return Optional.ofNullable(new BufferedReader(new InputStreamReader(exec.getInputStream())).readLine());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Downloads the latest version of yt-dlp into the current working-directory.
     * Note: this will currently only work with windows and linux
     */
    public static synchronized void downloadYTDLP() {
        String osName = System.getProperty("os.name").toLowerCase();

        String downloadUrl;

        if (osName.contains("linux")) {
            downloadUrl = YT_DLP_LINUX_DOWNLOAD_URL;
        } else if (osName.contains("windows")) {
            downloadUrl = YT_DLP_WINDOWS_DOWNLOAD_URL;
        } else {
            log.warn("Could not find yt-dlp binary for your operating-system! Pleas provide yt-dlp on your own!");
            return;
        }

        try {
            URL url = new URL(downloadUrl);
            File destination = new File(new File(url.getFile()).getName());
            FileUtils.copyURLToFile(url, destination);
            log.info("Downloaded yt-dlp to {}", destination.getAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not download yt-dlp!", e);
        }
    }

    /**
     * Load an url that is compatible with yt-dlp.
     * This will return a playlist if this is the case.
     * Otherwise, it will return a track.
     * @param url url of playlist or track
     * @return {@link YTDLPAudioPlaylist} if playlist and {@link YTDLPAudioTrack} otherwise
     */
    public static YTDLPAudioItem loadUrl(String url) throws MalformedURLException {
        return loadUrl(url, null);
    }

    /**
     * Load an url that is compatible with yt-dlp.
     * This will return a playlist if this is the case.
     * Otherwise, it will return a track.
     * @param url url of playlist or track
     * @param userData arbitrary user data
     * @return {@link YTDLPAudioPlaylist} if playlist and {@link YTDLPAudioTrack} otherwise
     */
    public static YTDLPAudioItem loadUrl(String url, Object userData) throws MalformedURLException {
        YTDLPAudioPlaylist playlist = new YTDLPAudioPlaylist(url, userData);
        playlist.load();

        if (playlist.getTracks().isEmpty()) {
            return new YTDLPAudioTrack(url, userData);
        }

        return playlist;
    }

}
