package org.dfhu.vpodplayer.feed;


import org.dfhu.vpodplayer.feed.Feed;

import java.util.List;

public interface FeedSubscriptions {
    void subscribe(Feed feed);
    void unsubscribe(int feedId);
    List<Feed> getFeeds();
}
