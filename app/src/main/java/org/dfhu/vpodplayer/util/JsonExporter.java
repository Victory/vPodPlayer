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

        Exported exported = new Exported();
        exported.shows = showsDb.all();
        exported.episodes = episodesDb.all();
        exported.now = System.currentTimeMillis();

        return gson.toJson(exported);
    }

    public static class Exported {
        List<Show> shows;
        List<Episode> episodes;
        long now;
    }
}
