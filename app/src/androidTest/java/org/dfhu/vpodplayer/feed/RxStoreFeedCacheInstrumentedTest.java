package org.dfhu.vpodplayer.feed;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import au.com.gridstone.rxstore.StoreProvider;
import au.com.gridstone.rxstore.converters.MoshiConverter;
import rx.Observer;

public class RxStoreFeedCacheInstrumentedTest extends Assert {

    public static final class TestClass {
        public final String something;
        public final String anotherThing;
        public final String thirdThing;

        public TestClass(String something,
                         String anotherThing,
                         String thirdThing) {
            this.something = something;
            this.anotherThing = anotherThing;
            this.thirdThing = thirdThing;
        }
    }

    @Test
    public void putTestClassWithStoreProvider() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        StoreProvider storeProvider =
                StoreProvider.withContext(appContext).inDir("test-rxstore").using(new MoshiConverter());

        TestClass tc = new TestClass("one", "two", "three");
        StoreProvider.ValueStore<TestClass> store = storeProvider.valueStore("test1", TestClass.class);

        final Throwable[] theE = {null};
        final Boolean[] completed = {false};
        final Boolean[] nexted = {false};

        store.observePut(tc).subscribe(new Observer<TestClass>() {
            @Override
            public void onCompleted() {
                completed[0] = true;
            }

            @Override
            public void onError(Throwable e) {
                theE[0] = e;
            }

            @Override
            public void onNext(TestClass testClass) {
                nexted[0] = true;
            }
        });

        // simple wait for observer just for testing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("thread interrupted");
        }

        assertNull(theE[0]);
        assertTrue(completed[0]);
        assertTrue(nexted[0]);
    }



    @Test
    public void putFeed() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        StoreProvider storeProvider =
                StoreProvider.withContext(appContext).inDir("test-rxstore").using(new MoshiConverter());


        String expectedTitle = "My title";
        List<CacheFeedItem> episodes = new ArrayList<>();
        episodes.add(new CacheFeedItem("episode 1", "http://example.com/link1.mp3"));
        episodes.add(new CacheFeedItem("episode 2", "http://example.com/link2.mp3"));
        CacheFeed cfi = new CacheFeed(expectedTitle, episodes);

        StoreProvider.ValueStore<CacheFeed> store = storeProvider.valueStore("put2", CacheFeed.class);

        final Throwable[] theE = {null};
        final Boolean[] completed = {false};
        final Boolean[] nexted = {false};

        store.observePut(cfi).subscribe(new Observer<CacheFeed>() {
            @Override
            public void onCompleted() {
                completed[0] = true;
            }

            @Override
            public void onError(Throwable e) {
                theE[0] = e;
            }

            @Override
            public void onNext(CacheFeed cacheFeed) {
                nexted[0] = true;
            }
        });

        // simple wait for observer just for testing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("thread interrupted");
        }

        assertNull(theE[0]);
        assertTrue(completed[0]);
        assertTrue(nexted[0]);

        Feed result = store.getBlocking();
        assertNotNull(result);
        assertEquals(result.getTitle(), expectedTitle);
        assertEquals(result.getItems().size(), 2);
        assertEquals(result.getItems().get(0).getTitle(), "episode 1");
        assertEquals(result.getItems().get(1).getTitle(), "episode 2");
    }

    @Test
    public void setterTestRxStoreFeedCache() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        StoreProvider storeProvider =
                StoreProvider.withContext(appContext).inDir("test-rxstore").using(new MoshiConverter());

        RxStoreFeedCache feedCache = new RxStoreFeedCache(storeProvider);

        String expectedTitle = "My title";
        List<CacheFeedItem> episodes = new ArrayList<>();
        episodes.add(new CacheFeedItem("episode 1", "http://example.com/link1.mp3"));
        episodes.add(new CacheFeedItem("episode 2", "http://example.com/link2.mp3"));
        Feed cfi = new CacheFeed(expectedTitle, episodes);

        final Throwable[] theE = {null};
        final Boolean[] completed = {false};
        final Boolean[] nexted = {false};

        feedCache.setFeed("testone", cfi).subscribe(new Observer<Feed>() {
            @Override
            public void onCompleted() {
                completed[0] = true;
            }

            @Override
            public void onError(Throwable e) {
                theE[0] = e;
            }

            @Override
            public void onNext(Feed cacheFeed) {
                nexted[0] = true;
            }
        });

        // simple wait for observer just for testing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("thread interrupted");
        }

        assertNull(theE[0]);
        assertTrue(completed[0]);
        assertTrue(nexted[0]);
    }

    @Test
    public void getterTestRxStoreFeedCache() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        StoreProvider storeProvider =
                StoreProvider.withContext(appContext).inDir("test-rxstore").using(new MoshiConverter());

        RxStoreFeedCache feedCache = new RxStoreFeedCache(storeProvider);

        String expectedTitle = "My title";
        List<CacheFeedItem> episodes = new ArrayList<>();
        episodes.add(new CacheFeedItem("episode 1", "http://example.com/link1.mp3"));
        episodes.add(new CacheFeedItem("episode 2", "http://example.com/link2.mp3"));
        Feed cfi = new CacheFeed(expectedTitle, episodes);

        final Throwable[] theE = {null};
        final Boolean[] completed = {false};
        final Boolean[] nexted = {false};

        feedCache.setFeed("testtwo", cfi).subscribe(new Observer<Feed>() {
            @Override
            public void onCompleted() {
                completed[0] = true;
            }

            @Override
            public void onError(Throwable e) {
                theE[0] = e;
            }

            @Override
            public void onNext(Feed cacheFeed) {
                nexted[0] = true;
            }
        });

        // simple wait for observer just for testing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("thread interrupted");
        }

        assertNull(theE[0]);
        assertTrue(completed[0]);
        assertTrue(nexted[0]);

        Feed result = feedCache.getFeed("testtwo");
        assertNotNull(result);
        assertEquals(result.getTitle(), expectedTitle);
        assertEquals(result.getItems().size(), 2);
        assertEquals(result.getItems().get(0).getTitle(), "episode 1");
        assertEquals(result.getItems().get(1).getTitle(), "episode 2");
    }
}
