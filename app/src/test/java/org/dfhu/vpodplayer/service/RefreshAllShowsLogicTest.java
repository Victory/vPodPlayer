package org.dfhu.vpodplayer.service;


import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.feed.Feed;
import org.dfhu.vpodplayer.feed.FeedFactory;
import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.observers.TestSubscriber;

import static org.dfhu.vpodplayer.feed.SubscriptionManagerTest.TEST_FEED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class RefreshAllShowsLogicTest extends Assert {

    private static final int SUBSCRIBE_TIMEOUT_MILLI = 13000;

    @Mock
    Context mockContext;

    @Mock
    NotificationManager mockNotificationManager;

    @Mock
    Shows mockShowsDb;

    @Mock
    Episodes mockEpisodesDb;

    @Mock
    StringsProvider stringsProvider;

    @Mock
    RefreshAllShowsService.RefreshAllShowsServiceNotification mockRefreshAllShowsServiceNotification;

    @Mock
    FeedFactory mockFeedFactory;

    private TestSubscriber<RefreshAllShowsLogic.RefreshResults> testSubscriber;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockShowsDb = mock(Shows.class);
        mockEpisodesDb = mock(Episodes.class);
        stringsProvider = mock(StringsProvider.class);
        mockRefreshAllShowsServiceNotification = mock(RefreshAllShowsService.RefreshAllShowsServiceNotification.class);
        testSubscriber = new TestSubscriber<>();
        mockFeedFactory = mock(FeedFactory.class);
    }

    private void tenShows() {

        InputStream inputStream = new ByteArrayInputStream(TEST_FEED.getBytes());
        List<Show> shows = new LinkedList<>();
        for (int ii = 0; ii < 10; ii++) {
            shows.add(new Show());
            shows.get(ii).title = "title " + ii;
            shows.get(ii).url = "http://example.com/" + ii;
            shows.get(ii).description = "description " + ii;
            shows.get(ii).id = 100 + ii;

            try {
                Feed feed = new FeedFactory().fromInputStream("http://example.com/" + ii, inputStream);
                when(mockFeedFactory.fromUrl("http://example.com/" + ii)).thenReturn(feed);
            } catch (IOException e) {
                fail("IO exception in mocking feed factory");
            }
        }

        when(mockShowsDb.all()).thenReturn(shows);
    }

    private RefreshAllShowsLogic buildRefreshAllShowLogic() {
        RefreshAllShowsLogic.Builder builder = new RefreshAllShowsLogic.Builder();

        SubscriptionManager subscriptionManager = new SubscriptionManager(
                mockFeedFactory,
                mockShowsDb,
                mockEpisodesDb);

        return builder
                .refreshAllShowsServiceNotification(mockRefreshAllShowsServiceNotification)
                .showsDb(mockShowsDb)
                .subscriptionManager(subscriptionManager)
                .stringsProvider(stringsProvider)
                .subscriber(testSubscriber)
                .build();
    }

    @Test
    @PrepareForTest(Log.class)
    public void showsDbAllIsCalled() {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        when(stringsProvider.getString(any(Integer.class))).thenReturn("MOCKED STRING");
        when(stringsProvider.getString(any(Integer.class), any(Object.class))).thenReturn("MOCKED STRING WITH ARGS");

        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();

        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);

        verify(mockShowsDb, times(1)).all();

    }

    @Test
    @PrepareForTest(Log.class)
    public void showNotificationsIsCalledForEachShowAndBeforeAndAfter() {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        when(stringsProvider.getString(any(Integer.class))).thenReturn("MOCKED STRING");
        when(stringsProvider.getString(any(Integer.class), any(Object.class))).thenReturn("MOCKED STRING WITH ARGS");

        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);

        verify(mockRefreshAllShowsServiceNotification, times(12))
                .show(any(String.class), any(String.class));
    }

    @Test
    @PrepareForTest(Log.class)
    public void showNotificationsIsCalledBeforeAndAfter() {
        PowerMockito.mockStatic(Log.class);

        when(stringsProvider.getString(R.string.app_name)).thenReturn("appname");
        when(stringsProvider.getQuantityString(R.plurals.numShowsUpdated, 0, 0)).thenReturn("none-updated");
        when(stringsProvider.getString(eq(R.string.updatingFeed), any(String.class))).thenReturn("3dots");

        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);

        verify(mockRefreshAllShowsServiceNotification, times(1))
                .show(eq("appname"), eq("3dots"));

        verify(mockRefreshAllShowsServiceNotification, times(1))
                .show(eq("appname"), eq("none-updated"));
    }

    @Test
    @PrepareForTest(Log.class)
    public void showNotificationsShowsTitle() {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        when(stringsProvider.getString(R.string.app_name)).thenReturn("appname");
        when(stringsProvider.getString(eq(R.string.updatingFeed), eq("title 5"))).thenReturn("update show 5");

        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI * 50, TimeUnit.MILLISECONDS);

        verify(mockRefreshAllShowsServiceNotification, atLeastOnce())
                .show(eq("appname"), eq("update show 5"));
    }

    @Test
    @PrepareForTest(Log.class)
    public void correctNumberOfShowsUpdate() {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        testSubscriber.assertTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        List<RefreshAllShowsLogic.RefreshResults> onNextEvents = testSubscriber.getOnNextEvents();
        RefreshAllShowsLogic.RefreshResults refreshResults = onNextEvents.get(0);
        assertTrue(refreshResults.numShowsUpdated.get() == 10);
    }

    @Test
    @PrepareForTest(Log.class)
    public void onNextIsOnlyCalledOnce() {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        testSubscriber.assertTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        List<RefreshAllShowsLogic.RefreshResults> onNextEvents = testSubscriber.getOnNextEvents();
        assertTrue("onNext should only be called once", onNextEvents.size() == 1);
    }


    @Test
    @PrepareForTest(Log.class)
    public void subscribeCalledForEachUrl() throws IOException {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        SubscriptionManager mockSubscriptionManager = mock(SubscriptionManager.class);

        RefreshAllShowsLogic logic =
                new RefreshAllShowsLogic.Builder()
                        .refreshAllShowsServiceNotification(mockRefreshAllShowsServiceNotification)
                        .showsDb(mockShowsDb)
                        .subscriptionManager(mockSubscriptionManager)
                        .stringsProvider(stringsProvider)
                        .subscriber(testSubscriber)
                        .build();

        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        verify(mockSubscriptionManager, times(10)).updateSubscription(any(String.class));
        verify(mockSubscriptionManager, times(1)).updateSubscription("http://example.com/5");
    }

    @Test
    @PrepareForTest(Log.class)
    public void pluralsForResultsPresent() throws IOException {
        PowerMockito.mockStatic(Log.class);
        tenShows();

        when(stringsProvider.getQuantityString(R.plurals.numShowsUpdated, 10, 10)).thenReturn("ten updated");
        RefreshAllShowsLogic logic = buildRefreshAllShowLogic();
        logic.handleIntent();
        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        verify(stringsProvider, times(1)).getQuantityString(R.plurals.numShowsUpdated, 10, 10);
    }

}
