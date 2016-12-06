package org.dfhu.vpodplayer.service;

import android.util.Log;

import junit.framework.Assert;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.feed.SubscriptionManager;
import org.dfhu.vpodplayer.model.Show;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.observers.TestSubscriber;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
public class SubscribeToShowLogicTest extends Assert {

    private static final int SUBSCRIBE_TIMEOUT_MILLI = 13000;

    @Mock
    Shows mockShowsDb;

    @Mock
    Episodes mockEpisodesDb;

    @Mock
    StringsProvider mockStringsProvider;

    /*
    @Mock
    FeedFactory mockFeedFactory;
    */

    @Mock
    SubscriptionManager mockSubscriptionManager;

    @Mock
    Notifier mockNotifier;

    private final String showUrl = "http://example.com/feed.xml";

    private static final String targetTitle = "targetTitle";
    private static final Show targetShow;
    static {
        targetShow = new Show();
        targetShow.title = targetTitle;
    }

    private TestSubscriber<SubscribeToShowService.Logic.Results> testSubscriber;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private SubscribeToShowService.Logic buildService() throws IOException {
        testSubscriber = new TestSubscriber<>();

        when(mockSubscriptionManager.subscribeToFeed(showUrl)).thenReturn(targetShow);

        return new SubscribeToShowService.Logic(
                showUrl, mockSubscriptionManager, mockNotifier)
                .setExtraResultsSubscriber(testSubscriber);
    }

    @Test
    @PrepareForTest(Log.class)
    public void testManagerIsCalledWithCorrectUrl() throws IOException {
        PowerMockito.mockStatic(Log.class);
        SubscribeToShowService.Logic logic = buildService();
        logic.handleIntent();

        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        testSubscriber.assertTerminalEvent();

        verify(mockSubscriptionManager, times(1)).subscribeToFeed(showUrl);
    }

    @Test
    @PrepareForTest(Log.class)
    public void testStartNotificationIsCalled() throws IOException {
        PowerMockito.mockStatic(Log.class);
        SubscribeToShowService.Logic logic = buildService();
        logic.handleIntent();

        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        testSubscriber.assertTerminalEvent();

        verify(mockNotifier, times(1)).packString(Notifier.TITLE, R.string.subscribingToShow);
        verify(mockNotifier, times(1)).packString(Notifier.CONTENT_TEXT, R.string.updatingFeed, showUrl);
        verify(mockNotifier, atLeastOnce()).show(SubscribeToShowService.NOTIFICATIONS_INDEX);
    }


    @Test
    @PrepareForTest(Log.class)
    public void testEndNotificationIsCalled() throws IOException {
        PowerMockito.mockStatic(Log.class);
        SubscribeToShowService.Logic logic = buildService();
        logic.handleIntent();

        testSubscriber.awaitTerminalEvent(SUBSCRIBE_TIMEOUT_MILLI, TimeUnit.MILLISECONDS);
        testSubscriber.assertTerminalEvent();

        verify(mockNotifier, times(1)).packString(Notifier.TITLE, R.string.newShowAdded);
        verify(mockNotifier, times(1)).packString(Notifier.CONTENT_TEXT, targetTitle);
        verify(mockNotifier, times(2)).show(SubscribeToShowService.NOTIFICATIONS_INDEX);
    }
}
