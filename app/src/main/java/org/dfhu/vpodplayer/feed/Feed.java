package org.dfhu.vpodplayer.feed;

import org.dfhu.vpodplayer.model.Episode;

import java.util.List;

public interface Feed {
    String getUrl();
    String getTitle();
    List<Episode> getEpisodes();
}
