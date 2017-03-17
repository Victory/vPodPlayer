package org.dfhu.vpodplayer.util;

import com.google.gson.Gson;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class JsonExporterTest {
    @Test
    public void export() throws Exception {

        List<Show> shows = new LinkedList<>();
        for (int ii = 0; ii < 3; ii ++) {
            Show show = new Show();
            show.id = ii;
            show.title = "Show " + ii;
            shows.add(show);
        }

        List<Episode> episodes = new LinkedList<>();

        int episodeId = 0;
        for (Show s: shows) {
            for (int ii = 0; ii < 3; ii++) {
                episodeId += 1;
                Episode episode = new Episode();
                episode.id = episodeId;
                episode.showId = s.id;
                episode.title = s.title + " Episode " + ii;
                episodes.add(episode);
            }
        }

        Shows showsDb = mock(Shows.class);
        when(showsDb.all()).thenReturn(shows);
        Episodes episodesDb = mock(Episodes.class);
        when(episodesDb.all()).thenReturn(episodes);

        JsonExporter jsonExporter = new JsonExporter(showsDb, episodesDb);
        String export = jsonExporter.export();

        Gson gson = new Gson();
        JsonExporter.ExportedPodcasts exportedPodcasts = gson.fromJson(export, JsonExporter.ExportedPodcasts.class);

        assertEquals(3, exportedPodcasts.shows.size());
        assertEquals("Show 2 Episode 1", exportedPodcasts.episodes.get(7).title);
    }


}