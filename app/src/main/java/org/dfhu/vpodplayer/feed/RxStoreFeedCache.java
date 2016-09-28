package org.dfhu.vpodplayer.feed;

import java.util.ArrayList;
import java.util.List;

import au.com.gridstone.rxstore.StoreProvider;
import rx.Single;

public class RxStoreFeedCache implements FeedCache {

    private final StoreProvider storeProvider;

    public RxStoreFeedCache(StoreProvider storeProvider) {
        this.storeProvider = storeProvider;
    }

    @Override
    public Single<Feed> setFeed(Feed feed) {
        List<CacheFeedItem> items = new ArrayList<>();

        for (FeedItem item: feed.getItems()) {
            items.add(new CacheFeedItem(item.getTitle(), item.getLink()));
        }
        CacheFeed cfi = new CacheFeed(feed.getUrl(), feed.getTitle(), items);
        StoreProvider.ValueStore<Feed> store = storeProvider.valueStore(cfi.getId(), CacheFeed.class);
        return store.observePut(cfi);
    }

    @Override
    public Feed getFeed(String feedId) {
        StoreProvider.ValueStore<Feed> store = storeProvider.valueStore(feedId, CacheFeed.class);
        return store.getBlocking();
    }

}
