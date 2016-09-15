package org.dfhu.vpodplayer.feed;

import au.com.gridstone.rxstore.StoreProvider;

public class RxStoreFeedCache implements FeedCache {

    private final StoreProvider storeProvider;

    public RxStoreFeedCache(StoreProvider storeProvider) {
        this.storeProvider = storeProvider;
    }

    @Override
    public void setFeed(String feedId, Feed feed) {

    }

    @Override
    public Feed getFeed(String feedId) {
        return null;
    }

}
