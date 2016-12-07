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

    public static class SubscribeResults {
        public Show show;
        public boolean isNew;
    }

    /** Store show and episodes from the feed in the database */
    private SubscribeResults subscribe(Feed feed, Shows showsDb, Episodes episodesDb) {

        SubscribeResults subscribeResults = new SubscribeResults();

        Show show = new Show();
        show.title = feed.getTitle();
        show.url = feed.getUrl();

        long result = showsDb.add(show);
        if (result < 0) {
            show = showsDb.findShowByUrl(show.url);
            subscribeResults.isNew = false;
        } else {
            show.id = (int) result;
            subscribeResults.isNew = true;
        }

        subscribeResults.show = showsDb.getById(show.id);

        if (show.url == null) {
            safeToast("Internal error: Can't addOrUpdate or find local subscription to show.");
            return subscribeResults;
        }

        List<Episode> episodes = feed.getEpisodes();
        episodesDb.addAllForShow(episodes, show.id);


        return subscribeResults;
    }

    /** Get a brand new feed or update episodes of an existing one */
    public SubscribeResults updateSubscription(String url) throws IOException {
        Feed feed = feedFactory.fromUrl(url);
        return subscribe(feed, showsDb, episodesDb);
    }
}
