package org.dfhu.vpodplayer.feed;

import java.util.List;

public interface Feed {
    String getTitle();

    List<FeedItem> getItems();
}
