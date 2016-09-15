package org.dfhu.vpodplayer.feed;

public class CacheFeedItem implements FeedItem {

    public final String title;
    public final String link;

    public CacheFeedItem(String title,
                         String link) {
        this.title = title;
        this.link = link;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getLink() {
        return link;
    }
}
