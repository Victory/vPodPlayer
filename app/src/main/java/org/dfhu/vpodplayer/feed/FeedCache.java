package org.dfhu.vpodplayer.feed;

import rx.Single;

/**
 * Set and Get the feed from locale storage
 */
public interface FeedCache {
    Feed getFeed(String feedId);
    Single<Feed> setFeed(Feed feed);
}
