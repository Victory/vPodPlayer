package org.dfhu.vpodplayer.feed;

import java.util.ArrayList;
import java.util.List;

public class CacheFeed implements Feed {

    public final String title;
    public final List<CacheFeedItem> items;

    public CacheFeed(String title,
                     List<CacheFeedItem> items) {
        this.title = title;
        this.items = items;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<FeedItem> getItems() {
        List<FeedItem> theItems = new ArrayList<>();
        for (FeedItem item: items) {
            theItems.add(item);
        }
        return theItems;
    }
}
