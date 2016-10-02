package org.dfhu.vpodplayer.sqlite;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.junit.Test;

import java.util.List;

public class EpisodesTest extends AndroidTestCase {
    private Episodes db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new Episodes(context);
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
    }

    @Test
    public void testAddWithGoodUrlAndGoodShowIdReturnsInAll() {
        Episode episode = new Episode();
        episode.title = "The title";
        episode.url = "http://example.com/episode1.mp3";
        episode.showId = 1;
        long id = db.add(episode);
        assertTrue(id > 0);

        List<Episode> all = db.all();
        assertTrue(all.contains(episode));
    }

    @Test
    public void testAddWithNullUrlReturnsNegativeOne() {
        Episode episode = new Episode();
        episode.title = "The title";
        episode.url = null;
        episode.showId = 1;
        long result = db.add(episode);
        assertEquals(-1, result);
    }

    @Test
    public void testAddWithMissingShowIdUrlReturnsNegativeOne() {
        Episode episode = new Episode();
        episode.title = "The title";
        episode.url = "http://example.com/episode1.mp3";
        long result = db.add(episode);
        assertEquals(-1, result);
    }


    @Test
    public void testAllByShowId() {
        Episode episode = new Episode();
        episode.title = "title 1";
        episode.url = "http://example.com/episode1.mp3";
        episode.showId = 1;
        long id = db.add(episode);
        assertTrue(id > 0);

        Episode episode2 = new Episode();
        episode2.title = "title 2";
        episode2.url = "http://example.com/episode2.mp3";
        episode2.showId = 1;
        id = db.add(episode2);
        assertTrue(id > 0);

        Episode otherShow = new Episode();
        otherShow.title = "other show title 1";
        otherShow.url = "http://example.com/other-show/episode1.mp3";
        otherShow.showId = 2;
        id = db.add(otherShow);
        assertTrue(id > 0);

        List<Episode> all = db.allForShow(1);
        assertEquals(2, all.size());
        assertTrue(all.contains(episode2));
        assertTrue(!all.contains(otherShow));
    }
}
