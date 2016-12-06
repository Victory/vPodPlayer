package org.dfhu.vpodplayer.service;

import org.dfhu.vpodplayer.util.StringsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class NotifierTest extends Assert {

    @Mock
    StringsProvider stringsProvider;

    @Mock
    Notifier.NotificationWrapper notificationWrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCallsWithCorrectIndex() {
        Notifier notifier = new Notifier(notificationWrapper, stringsProvider);
        int target = 101;
        notifier.show(target);
        verify(notificationWrapper, times(1)).show(target, null, null);
    }

    @Test
    public void testCallsWithCorrectTitleId() {
        Notifier notifier = new Notifier(notificationWrapper, stringsProvider);
        int target = 201;
        notifier.packString(Notifier.TITLE, target);
        verify(stringsProvider, times(1)).getString(target);
    }

    @Test
    public void testCallsWithCorrectContentTextId() {
        Notifier notifier = new Notifier(notificationWrapper, stringsProvider);
        int target = 201;
        notifier.packString(Notifier.CONTENT_TEXT, target);
        verify(stringsProvider, times(1)).getString(target);
    }

    @Test
    public void testCallsWithCorrectContentAndText() {
        Notifier notifier = new Notifier(notificationWrapper, stringsProvider);
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
