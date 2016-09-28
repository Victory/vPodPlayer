package org.dfhu.vpodplayer.feed;

import java.util.List;

public interface Feed {
    String getId();
    String getUrl();
    String getTitle();
    List<FeedItem> getItems();
}
