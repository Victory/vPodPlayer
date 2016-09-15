package org.dfhu.vpodplayer.feed;

/**
 * Set and Get the feed from locale storage
 */
public interface FeedCache {
    Feed getFeed(String feedId);
    void setFeed(String feedId, Feed feed);
}
