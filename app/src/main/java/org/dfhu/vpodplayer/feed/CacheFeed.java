package org.dfhu.vpodplayer.feed;

import java.util.ArrayList;
import java.util.List;

public class CacheFeed implements Feed {
    private final String title;
    private final List<CacheFeedItem> items;
    private final String url;

    public CacheFeed(String url,
                     String title,
                     List<CacheFeedItem> items) {
        this.title = title;
        this.items = items;
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getId() {
        return "V1-" + getUrl().toLowerCase().replaceAll("[^a-zA-Z0-9]", "-");
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
