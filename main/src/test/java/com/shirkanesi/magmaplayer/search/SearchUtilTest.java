package com.shirkanesi.magmaplayer.search;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchUtilTest {

    @Test
    void testSearchValid() {
        try {
            List<SearchResult> results = SearchUtil.search("ytsearch10", "music");

            assertEquals(10, results.size());

            for (SearchResult result : results) {
                assertNotNull(result);
                assertNotNull(result.getTitle());
                assertNotNull(result.getUrl());
            }
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testSearchSingle() {
        try {
            List<SearchResult> results = SearchUtil.search("ytsearch", "music");

            assertEquals(1, results.size());

            assertNotNull(results.get(0));
            assertNotNull(results.get(0).getTitle());
            assertNotNull(results.get(0).getUrl());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testSearchSingleMultipleWords() {
        try {
            List<SearchResult> results = SearchUtil.search("ytsearch", "edm music");

            assertEquals(1, results.size());

            assertNotNull(results.get(0));
            assertNotNull(results.get(0).getTitle());
            assertNotNull(results.get(0).getUrl());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testSearchInvalid() {
        try {
            List<SearchResult> results = SearchUtil.search("ytsearch10", "hikodfgsoihsdghgoujuoihöfdgs");

            assertEquals(0, results.size());
        } catch (IOException e) {
            fail(e);
        }
    }
}