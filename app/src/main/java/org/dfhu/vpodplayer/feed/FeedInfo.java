package org.dfhu.vpodplayer.feed;

import java.util.ArrayList;
import java.util.List;

public class FeedInfo {
    private final List<EpisodeInfo> episodes = new ArrayList<>();
    private String title;

    public List<EpisodeInfo> getEpisodes() {
        return episodes;
    }

    public void addEpisode(EpisodeInfo episode) {
        episodes.add(episode);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
