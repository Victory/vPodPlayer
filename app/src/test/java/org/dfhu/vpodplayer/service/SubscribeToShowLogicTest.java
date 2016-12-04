package org.dfhu.vpodplayer.service;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import junit.framework.Assert;

import org.dfhu.vpodplayer.feed.FeedFactory;
import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import rx.observers.TestSubscriber;

import static org.powermock.api.mockito.PowerMockito.mock;


@RunWith(PowerMockRunner.class)
public class SubscribeToShowLogicTest extends Assert {

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
    SubscribeToShowService.SubscribeToShowServiceNotification subscribeToShowServiceNotification;

    @Mock
    FeedFactory mockFeedFactory;

    final String showUrl = "http://example.com/feed.xml";

    private TestSubscriber<SubscribeToShowLogic.SubscribeResult> testSubscriber;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private SubscribeToShowLogic buildService() {
        mockFeedFactory = mock(FeedFactory.class);

        SubscriptionManager subscriptionManager = new SubscriptionManager.Builder()
                .showsDb(mockShowsDb)
                .episodesDb(mockEpisodesDb)
                .feedFactory(mockFeedFactory)
                .build();

        return new SubscribeToShowLogic.Builder()
                .subscribeToShowServiceNotification(subscribeToShowServiceNotification)
                .showUrl(showUrl)
                .subscriptionManager(subscriptionManager)
                .stringsProvider(stringsProvider)
                .build();

    }

    @Test
    public void testBuild() {
        buildService();
    }

    @Test
    @PrepareForTest(Log.class)
    public void testManagerIsCalledWithCorrectUrl() {
        PowerMockito.mockStatic(Log.class);
        mockFeedFactory = mock(FeedFactory.class);

        SubscribeToShowLogic subscribeToShowLogic = buildService();
        subscribeToShowLogic.handleIntent();
    }
}
