package org.dfhu.vpodplayer.sqlite;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import org.dfhu.vpodplayer.model.Show;
import org.junit.Test;

import java.util.List;

public class ShowsTest extends AndroidTestCase {
    private Shows db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new Shows(context);
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
    }

    @Test
    public void testAddWithGoodUrlReturnsInAll() {
        Show show = new Show();
        show.title = "The title";
        show.url = "http://example.com/feed.xml";
        long id = db.add(show);
        assertTrue(id > 0);

        List<Show> all = db.all();
        assertTrue(all.contains(show));

    }


    @Test
    public void testAddWithNullUrlReturnsNegativeOne() {
        Show show = new Show();
        show.title = "The title";
        show.url = null;
        long result = db.add(show);
        assertEquals(-1, result);
    }
}
