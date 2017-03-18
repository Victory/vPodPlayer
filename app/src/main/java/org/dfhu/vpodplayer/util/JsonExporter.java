package org.dfhu.vpodplayer.util;

import com.google.gson.Gson;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;

import java.util.List;

public class JsonExporter {

    private final Shows showsDb;
    private final Episodes episodesDb;

    public JsonExporter(Shows showsDb, Episodes episodesDb) {
        this.showsDb = showsDb;
        this.episodesDb = episodesDb;
    }

    /**
     * Gets a json representation of the shows and episodes
     */
    public String export() {
        Gson gson = new Gson();

        ExportedPodcasts exportedPodcasts = new ExportedPodcasts();
        exportedPodcasts.shows = showsDb.all();
        exportedPodcasts.episodes = episodesDb.all();
        exportedPodcasts.now = System.currentTimeMillis();

        return gson.toJson(exportedPodcasts);
    }

    static class ExportedPodcasts {
        List<Show> shows;
        List<Episode> episodes;
        long now;
    }
}
