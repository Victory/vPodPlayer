package org.dfhu.vpodplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import org.dfhu.vpodplayer.util.StringsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class NotifierTest extends Assert {

    @Mock
    Context applicationContext;

    @Mock
    StringsProvider stringsProvider;

    @Mock
    NotificationManager notificationManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @PrepareForTest(Notifier.NotificationBuilderWrapper.class)
    public void testCallsWithCorrectIndex() {
        PowerMockito.mockStatic(Notifier.NotificationBuilderWrapper.class);

        Notifier notifier = new Notifier(applicationContext, stringsProvider, notificationManager);
        int target = 101;
        notifier.show(target);
        verify(notificationManager, times(1)).notify(eq(target), any(Notification.class));
    }

    @Test
    @PrepareForTest(Notifier.NotificationBuilderWrapper.class)
    public void testCallsWithCorrectTitleId() {
        PowerMockito.mockStatic(Notifier.NotificationBuilderWrapper.class);
        Notifier notifier = new Notifier(applicationContext, stringsProvider, notificationManager);
        int target = 201;
        notifier.packString(Notifier.TITLE, target);
        verify(stringsProvider, times(1)).getString(target);
    }

    @Test
    @PrepareForTest(Notifier.NotificationBuilderWrapper.class)
    public void testCallsWithCorrectContentTextId() {
        PowerMockito.mockStatic(Notifier.NotificationBuilderWrapper.class);
        Notifier notifier = new Notifier(applicationContext, stringsProvider, notificationManager);
        int target = 201;
        notifier.packString(Notifier.CONTENT_TEXT, target);
        verify(stringsProvider, times(1)).getString(target);
    }

    @Test
    @PrepareForTest(Notifier.NotificationBuilderWrapper.class)
    public void testCallsWithCorrectContentAndText() {
        PowerMockito.mockStatic(Notifier.NotificationBuilderWrapper.class);
        Notifier notifier = new Notifier(applicationContext, stringsProvider, notificationManager);
        int titleTarget = 201;
        int contentTarget = 301;
        when(stringsProvider.getString(titleTarget)).thenReturn("title");
        when(stringsProvider.getString(contentTarget)).thenReturn("content");
        notifier.packString(Notifier.CONTENT_TEXT, contentTarget);
        notifier.packString(Notifier.TITLE, titleTarget);

        assertEquals(notifier.getPackedString(Notifier.CONTENT_TEXT), "content");
        assertEquals(notifier.getPackedString(Notifier.TITLE), "title");
    }
}
