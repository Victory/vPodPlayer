package org.dfhu.vpodplayer.feed;


import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;

import java.io.IOException;
import java.util.List;

import static org.dfhu.vpodplayer.VPodPlayer.safeToast;

public class SubscriptionManager {

    private final Shows showsDb;
    private final Episodes episodesDb;
    private final FeedFactory feedFactory;

    public SubscriptionManager(FeedFactory feedFactory, Shows showsDb, Episodes episodesDb) {
        this.showsDb = showsDb;
        this.episodesDb = episodesDb;
        this.feedFactory = feedFactory;
    }

    /** Store show and episodes from the feed in the database */
    private Show subscribe(Feed feed, Shows showsDb, Episodes episodesDb) {

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

    /** Get a brand new feed */
    public Show subscribeToFeed(String url) throws IOException {
        Feed feed = feedFactory.fromUrl(url);
        return subscribe(feed, showsDb, episodesDb);
    }

    /** Get list of new episodes */
    public void refreshFeed(String url) throws IOException {
        Feed feed = feedFactory.fromUrl(url);
        subscribe(feed, showsDb, episodesDb);
    }
}
