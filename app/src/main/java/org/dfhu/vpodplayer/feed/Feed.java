package org.dfhu.vpodplayer.feed;

import java.util.List;

interface Feed {
    String getTitle();

    List<FeedItem> getItems();
}
