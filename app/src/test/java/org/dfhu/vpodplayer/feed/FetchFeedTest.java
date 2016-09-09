package org.dfhu.vpodplayer.feed;


import android.util.Log;

import junit.framework.Assert;

import org.dfhu.vpodplayer.util.VicURL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class FetchFeedTest extends Assert {

    @Mock
    URLConnection mockConn;

    @Mock
    VicURL mockUrl;

    @Test(expected=IllegalAccessException.class)
    public void constructorIsPrivate() throws Exception {
        Constructor<?>[] constructors = FetchFeed.class.getDeclaredConstructors();
        assertTrue(constructors.length == 1);
        constructors[0].setAccessible(true);
        constructors[0].newInstance().getClass();

        FetchFeed.class.newInstance();
        fail("Method should be private");
    }

    @Test
    public void goodUrlWillReturnSuccessStatus() throws IOException {
        mockUrl = mock(VicURL.class);
        mockConn = mock(URLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConn);

        FeedFetchResult results = FetchFeed.fetch(mockUrl);

        assertTrue(results.status == FeedFetchResult.Status.SUCCESS);
    }

    @Test
    @PrepareForTest(android.util.Log.class)
    public void badConnectionWillReturnFailureStatus() throws IOException {
        mockUrl = mock(VicURL.class);
        when(mockUrl.openConnection()).thenThrow(new IOException());
        PowerMockito.mockStatic(Log.class);

        FeedFetchResult results = FetchFeed.fetch(mockUrl);

        assertTrue(results.status == FeedFetchResult.Status.COULD_NOT_OPEN_URL);
    }

    @Test
    @PrepareForTest(android.util.Log.class)
    public void badInputStreamWillReturnFailureStatus() throws IOException {
        mockUrl = mock(VicURL.class);
        mockConn = mock(URLConnection.class);
        when(mockConn.getInputStream()).thenThrow(new IOException());
        when(mockUrl.openConnection()).thenReturn(mockConn);

        PowerMockito.mockStatic(Log.class);

        FeedFetchResult results = FetchFeed.fetch(mockUrl);

        assertTrue(results.status == FeedFetchResult.Status.COULD_NOT_GET_INPUTSTREAM);
    }
}
