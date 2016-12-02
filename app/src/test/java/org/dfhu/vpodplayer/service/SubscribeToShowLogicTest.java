package org.dfhu.vpodplayer.service;

import android.app.NotificationManager;
import android.content.Context;

import junit.framework.Assert;

import org.dfhu.vpodplayer.feed.FeedFactory;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import rx.observers.TestSubscriber;


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
    RefreshAllShowsService.RefreshAllShowsServiceNotification mockRefreshAllShowsServiceNotification;

    @Mock
    FeedFactory mockFeedFactory;

    private TestSubscriber<RefreshAllShowsLogic.RefreshResults> testSubscriber;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBuild() {
        fail("TODO");

    }

}
