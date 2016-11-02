package org.dfhu.vpodplayer.feed;


import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.MediaDuration;

import java.util.List;

import static org.dfhu.vpodplayer.VPodPlayer.safeToast;

public class SubscribeToFeed {

    private SubscribeToFeed() {}

    /** Store show and episdoes from the feed in the database */
    public static Show subscribe(Feed feed, Shows showsDb, Episodes episodesDb) {

        Show show = new Show();
        show.title = feed.getTitle();
        show.url = feed.getUrl();

        long result = showsDb.add(show);
        if (result < 0) {
            show = showsDb.findShowByUrl(show.url);
        } else {
            show.id = (int) result;
        }

        if (show.url == null) {
            safeToast("Internal error: Can't addOrUpdate or find local subscription to show.");
            return show;
        }

        List<Episode> episodes = feed.getEpisodes();
        episodesDb.addAllForShow(episodes, show.id);

        return show;
    }
}
