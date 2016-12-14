package org.dfhu.vpodplayer.service;

import android.net.Uri;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.util.PathsUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EpisodeDownloaderTest extends Assert {

    @Mock
    EpisodeDownloader.DownloadManagerWrapper mockDownloadManagerWrapper;

    @Mock
    PathsUtility mockPathsUtility;

    private EpisodeDownloader episodeDownloader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        episodeDownloader = new EpisodeDownloader(mockDownloadManagerWrapper, mockPathsUtility);
    }

    @Test
    public void enqueue() {
        final Episode episode = new Episode();

        episode.showId = 100;
        episode.id = 10;
        episode.url = "http://example.com/episode.mp3";


        // Uri.parse() would return a "not mocked" error
        final Uri episodeUri = mock(Uri.class);
        final File dir = new File("/some/place/on/disk");
        final Uri destinationDir = mock(Uri.class);

        when(mockPathsUtility.stringToUri(episode.url))
                .thenReturn(episodeUri);
        when(mockPathsUtility.makeExternalFilesDirChildDirs("episodes", "show-100"))
                .thenReturn(dir);
        when(mockPathsUtility.conditionalCreateDir(dir))
                .thenReturn(true);
        when(mockPathsUtility.fileToUri(new File(dir, "10.mp3")))
                .thenReturn(destinationDir);
        when(mockDownloadManagerWrapper.enqueue(episode, episodeUri, destinationDir))
                .thenReturn(99L);

        long result = episodeDownloader.enqueue(episode);

        assertEquals(99L, result);
        verify(mockDownloadManagerWrapper, times(1)).enqueue(episode, episodeUri, destinationDir);
        verify(mockPathsUtility, times(1)).stringToUri(episode.url);
        verify(mockPathsUtility, times(1)).makeExternalFilesDirChildDirs("episodes", "show-100");
        verify(mockPathsUtility, times(1)).conditionalCreateDir(dir);
        verify(mockPathsUtility, times(1)).fileToUri(new File(dir, "10.mp3"));

    }

    @Test
    public void enqueueCannotCreateDirectory() {
        final Episode episode = new Episode();

        episode.showId = 100;
        episode.id = 10;
        episode.url = "http://example.com/episode.mp3";


        // Uri.parse() would return a "not mocked" error
        final Uri episodeUri = mock(Uri.class);
        final File dir = new File("/some/place/on/disk");
        final Uri destinationDir = mock(Uri.class);

        when(mockPathsUtility.stringToUri(episode.url))
                .thenReturn(episodeUri);
        when(mockPathsUtility.makeExternalFilesDirChildDirs("episodes", "show-100"))
                .thenReturn(dir);
        when(mockPathsUtility.conditionalCreateDir(dir))
                .thenReturn(false);
        when(mockPathsUtility.fileToUri(new File(dir, "10.mp3")))
                .thenReturn(destinationDir);
        when(mockDownloadManagerWrapper.enqueue(episode, episodeUri, destinationDir))
                .thenReturn(-1L);

        long result = episodeDownloader.enqueue(episode);

        assertEquals(-1L, result);
        verify(mockDownloadManagerWrapper, never()).enqueue(any(Episode.class), any(Uri.class), any(Uri.class));
        verify(mockPathsUtility, times(1)).conditionalCreateDir(dir);
        verify(mockPathsUtility, never()).fileToUri(new File(dir, "10.mp3"));

    }

}
