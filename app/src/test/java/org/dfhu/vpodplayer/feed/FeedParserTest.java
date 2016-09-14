package org.dfhu.vpodplayer.feed;


import android.util.Log;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
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

    /*
    @Test
    public void itemHasTitleAndLink() {

        List<FeedItem> items = new ArrayList<>();

        mockFeed = mock(Feed.class);
        FeedItem mockItem = mock(FeedItem.class);
        String itemTitle = "Item Title";
        when(mockItem.getTitle()).thenReturn(itemTitle);
        FeedItemEnclosure mockEnclosure = mock(FeedItemEnclosure.class);
        List<FeedItemEnclosure> enclosures = new ArrayList<>();
        when(mockEnclosure.getType()).thenReturn("audio/");
        enclosures.add(mockEnclosure);
        String link = "http://mysite.com/episode1.mp3";
        when(mockEnclosure.getLink()).thenReturn(link);

        Mockito.<List<? extends FeedItemEnclosure>>when(mockItem.getLink()).thenReturn(enclosures);
        items.add(mockItem);
        Mockito.<List<? extends FeedItem>>when(mockFeed.getItems()).thenReturn(items);

        FeedParserResult result = FeedParser.parse(mockFeed);

        assertTrue(result.status == FeedParserResult.Status.SUCCESS);
        assertEquals(itemTitle, result.info.getEpisodes().get(0).getTitle());
        assertEquals(link, result.info.getEpisodes().get(0).getLink());
    }

    @Test
    @PrepareForTest(android.util.Log.class)
    public void badEpisodesDontBreakGoodEpisodes() {

        PowerMockito.mockStatic(Log.class);

        List<FeedItem> items = new ArrayList<>();

        // mock a "bad" item
        items.add(mock(FeedItem.class));

        // mock a "good" item
        mockFeed = mock(Feed.class);
        FeedItem mockItem = mock(FeedItem.class);
        String itemTitle = "Item Title";
        when(mockItem.getTitle()).thenReturn(itemTitle);
        FeedItemEnclosure mockEnclosure = mock(FeedItemEnclosure.class);
        List<FeedItemEnclosure> enclosures = new ArrayList<>();
        when(mockEnclosure.getType()).thenReturn("audio/");
        String link = "http://mysite.com/episode1.mp3";
        when(mockEnclosure.getLink()).thenReturn(link);
        enclosures.add(mockEnclosure);
        Mockito.<List<? extends FeedItemEnclosure>>when(mockItem.getEnclosures()).thenReturn(enclosures);
        items.add(mockItem);

        // mock another "bad" item
        items.add(mock(FeedItem.class));

        Mockito.<List<? extends FeedItem>>when(mockFeed.getItems()).thenReturn(items);

        FeedParserResult result = FeedParser.parse(mockFeed);

        PowerMockito.verifyStatic();
        assertTrue(result.status == FeedParserResult.Status.SUCCESS);
        assertEquals(itemTitle, result.info.getEpisodes().get(1).getTitle());
        assertEquals(link, result.info.getEpisodes().get(1).getLink());
        assertEquals(3, result.info.getEpisodes().size());
    }


    @Test
    @PrepareForTest(android.util.Log.class)
    public void itemsHaveAnId() {

        PowerMockito.mockStatic(Log.class);

        List<FeedItem> items = new ArrayList<>();


        // mock a "good" item
        FeedItem mockItem = mock(FeedItem.class);
        String itemTitle = "Item Title 1";
        when(mockItem.getTitle()).thenReturn(itemTitle);
        FeedItemEnclosure mockEnclosure = mock(FeedItemEnclosure.class);
        List<FeedItemEnclosure> enclosures = new ArrayList<>();
        when(mockEnclosure.getType()).thenReturn("audio/");
        String link1 = "http://mysite.com/episode1.mp3";
        when(mockEnclosure.getLink()).thenReturn(link1);
        enclosures.add(mockEnclosure);
        Mockito.<List<? extends FeedItemEnclosure>>when(mockItem.getEnclosures()).thenReturn(enclosures);
        items.add(mockItem);

        // mock another "good" item
        mockItem = mock(FeedItem.class);
        itemTitle = "Item Title 2";
        when(mockItem.getTitle()).thenReturn(itemTitle);
        mockEnclosure = mock(FeedItemEnclosure.class);
        enclosures = new ArrayList<>();
        when(mockEnclosure.getType()).thenReturn("audio/");
        String link2 = "http://mysite.com/episode2.mp3";
        when(mockEnclosure.getLink()).thenReturn(link2);
        enclosures.add(mockEnclosure);
        Mockito.<List<? extends FeedItemEnclosure>>when(mockItem.getEnclosures()).thenReturn(enclosures);
        items.add(mockItem);

        mockFeed = mock(Feed.class);
        Mockito.<List<? extends FeedItem>>when(mockFeed.getItems()).thenReturn(items);

        FeedParserResult result = FeedParser.parse(mockFeed);

        PowerMockito.verifyStatic();
        assertTrue(result.status == FeedParserResult.Status.SUCCESS);
        assertEquals(itemTitle, result.info.getEpisodes().get(1).getTitle());
        assertEquals(link1, result.info.getEpisodes().get(0).getLink());
        assertEquals(link2, result.info.getEpisodes().get(1).getLink());
        assertTrue(result.info.getEpisodes().get(0).getId() != null);
        assertTrue(!result.info.getEpisodes().get(0).getId().isEmpty());
        assertTrue(!result.info.getEpisodes().get(1).getId().isEmpty());
    }
    */
}