package org.dfhu.vpodplayer.feed;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class SubscriptionManagerTest extends Assert {

    @Mock
    Shows mockShowsDb;

    @Mock
    Episodes mockEpisodesDb;

    public static final String TEST_FEED = "<rss>" +
            "<channel>" +
            "<title>Test title</title>" +
            "<item><title>Title 0</title></item>" +
            "<item><title>Title 1</title></item>" +
            "<item><title>Title 2</title></item>" +
            "</channel>" +
            "</rss>";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testShowDbMethodsCalledOnRefresh() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(TEST_FEED.getBytes());

        when(mockShowsDb.add(any(Show.class))).thenReturn(2L);

        Feed feed = new FeedFactory().fromInputStream("http://example.com/0", inputStream);
        FeedFactory mockFeedFactory = mock(FeedFactory.class);
        when(mockFeedFactory.fromUrl("http://example.com/0")).thenReturn(feed);

        SubscriptionManager subscriptionManager = new SubscriptionManager(mockFeedFactory, mockShowsDb, mockEpisodesDb);

        subscriptionManager.updateSubscription("http://example.com/0");

        ArgumentCaptor<Show> arg = ArgumentCaptor.forClass(Show.class);
        verify(mockShowsDb).add(arg.capture());
        assertEquals(arg.getValue().title, "Test title");
        assertEquals(arg.getValue().url, "http://example.com/0");
        verify(mockShowsDb, never()).findShowByUrl(any(String.class));
    }

    @Test
    public void testAllEpisodesAreCalled() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(TEST_FEED.getBytes());

        when(mockShowsDb.add(any(Show.class))).thenReturn(2L);

        Feed feed = new FeedFactory().fromInputStream("http://example.com/0", inputStream);
        FeedFactory mockFeedFactory = mock(FeedFactory.class);
        when(mockFeedFactory.fromUrl("http://example.com/0")).thenReturn(feed);


        SubscriptionManager subscriptionManager = new SubscriptionManager(mockFeedFactory, mockShowsDb, mockEpisodesDb);
        subscriptionManager.updateSubscription("http://example.com/0");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ArrayList<Episode>> arg = ArgumentCaptor.forClass((Class) List.class);
        verify(mockEpisodesDb).addAllForShow(arg.capture(), eq(2));
        assertEquals(arg.getValue().get(1).title, "Title 1");
        assertEquals(arg.getValue().size(), 3);
    }

}
