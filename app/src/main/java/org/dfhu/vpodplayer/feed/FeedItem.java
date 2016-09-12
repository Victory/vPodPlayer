package org.dfhu.vpodplayer.feed;

import java.util.List;

public interface FeedItem {
    String getTitle();

    List<FeedItemEnclosure> getEnclosures();
}
