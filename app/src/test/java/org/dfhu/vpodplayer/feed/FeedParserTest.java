package org.dfhu.vpodplayer.feed;


import com.einmalfel.earl.Feed;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedParserTest extends Assert {

    @Mock
    Feed mockFeed;

    @Test
    public void hasSuccessStatus() {
        mockFeed = mock(Feed.class);

        FeedParserResult result = FeedParser.parse(mockFeed);

        assertTrue(result.status == FeedParserResult.Status.SUCCESS);
    }

    @Test
    public void setsTitle() {
        mockFeed = mock(Feed.class);
        when(mockFeed.getTitle()).thenReturn("Mock Podcast");
        FeedParserResult result = FeedParser.parse(mockFeed);

        assertTrue(result.status == FeedParserResult.Status.SUCCESS);
        assertEquals(result.info.getTitle(), "Mock Podcast");
    }
}
