package org.dfhu.vpodplayer.feed;


interface FeedItem {
    String getTitle();

    String getUrl();

    String getUniqueId();

    long getPubDate();
}
