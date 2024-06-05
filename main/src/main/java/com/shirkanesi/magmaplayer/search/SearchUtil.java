package com.shirkanesi.magmaplayer.search;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SearchUtil {

    private static final String SEARCH_COMMANDS = "yt-dlp %s:%s --print-json --flat-playlist --skip-download --no-warnings";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<SearchResult> search(String prefix, String query) throws IOException {
        final String findInformation = String.format(SEARCH_COMMANDS, prefix, query);
        Process process = Runtime.getRuntime().exec(findInformation);

        List<SearchResult> results = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                    results.add(objectMapper.readValue(line, SearchResult.class));
            }
        }

        return results;
    }

}