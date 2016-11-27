package org.dfhu.vpodplayer.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.StringsProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class RefreshAllShowsLogicTest extends Assert {
    @Mock
    Context mockContext;

    @Mock
    NotificationManager mockNotificationManager;

    @Mock
    Shows mockShowsDb;

    @Mock
    StringsProvider stringsProvider;

    @Mock
    RefreshAllShowsService.RefreshAllShowsServiceNotification mockRefreshAllShowsServiceNotification;

    @Test
    @PrepareForTest(Log.class)
    public void allShowsIsCalled() {
        PowerMockito.mockStatic(Log.class);

        mockContext = mock(Context.class);
        mockNotificationManager = mock(NotificationManager.class);
        mockShowsDb = mock(Shows.class);
        stringsProvider = mock(StringsProvider.class);
        mockRefreshAllShowsServiceNotification = mock(RefreshAllShowsService.RefreshAllShowsServiceNotification.class);

        mock(Notification.Builder.class);

        List<Show> shows = new LinkedList<>();
        shows.add(new Show());
        shows.get(0).title = "title 1";
        shows.get(0).url = "http://example.com/1.mp3";
        shows.get(0).description = "description 1";
        shows.get(0).id = 100;
        when(mockShowsDb.all()).thenReturn(shows);

        when(stringsProvider.getString(any(Integer.class))).thenReturn("MOCKED STRING");
        when(stringsProvider.getString(any(Integer.class), any(Object.class))).thenReturn("MOCKED STRING WITH ARGS");

        RefreshAllShowsLogic.Builder builder = new RefreshAllShowsLogic.Builder();
        RefreshAllShowsLogic logic = builder
                .refreshAllShowsServiceNotification(mockRefreshAllShowsServiceNotification)
                .showsDb(mockShowsDb)
                .stringsProvider(stringsProvider)
                .build();

        logic.handleIntent();

        verify(mockShowsDb, times(1)).all();
    }
}
