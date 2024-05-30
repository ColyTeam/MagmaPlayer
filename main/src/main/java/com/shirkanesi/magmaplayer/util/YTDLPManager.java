package com.shirkanesi.magmaplayer.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

@Slf4j
public final class YTDLPManager {

    private static final String YT_DLP_LINUX_DOWNLOAD_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
    private static final String YT_DLP_WINDOWS_DOWNLOAD_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";

    private static final YTDLPManager INSTANCE = new YTDLPManager();

    /**
     * Get the version of yt-dlp
     *
     * @return an optional containing the version-string of yt-dlp (or empty iff not found)
     */
    private synchronized Optional<String> getYTDLPVersion() {
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
    private synchronized void downloadYTDLP() {
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
     * Get the default {@link YTDLPManager}
     * @return the default {@link YTDLPManager}
     */
    public static YTDLPManager getDefault() {
        return YTDLPManager.INSTANCE;
    }

}