package org.dfhu.vpodplayer.feed;


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
        public List<Episode> newEpisodes;
    }

    /** Store show and episodes from the feed in the database */
    private SubscribeResults subscribe(Feed feed, Shows showsDb, Episodes episodesDb) {

        SubscribeResults subscribeResults = new SubscribeResults();

        Show show;
        show = showsDb.findShowByUrl(feed.getUrl());
        if (show != null) {
            subscribeResults.isNew = false;
        } else {
            show = new Show();
            show.title = feed.getTitle();
            show.url = feed.getUrl();
            long result = showsDb.add(show);
            show.id = (int) result;
            subscribeResults.isNew = true;
        }

        subscribeResults.show = showsDb.getById(show.id);

        if (show.url == null) {
            safeToast("Internal error: Can't addOrUpdate or find local subscription to show.");
            return subscribeResults;
        }

        List<Episode> episodes = feed.getEpisodes();
        List<Episode> newEps = episodesDb.addAllForShow(episodes, show.id);


        // regardless if any of the 5 latest episodes are not downloaded, count them as new
        int end = Math.min(newEps.size(), 5);
        for (Episode e: newEps.subList(0, end)) {
            if (!e.isDownloaded() && !newEps.contains(e) && e.percentListened == 0) {
                newEps.add(e);
            }
        }

        subscribeResults.newEpisodes = newEps;
        return subscribeResults;
    }

    /** Get a brand new feed or update episodes of an existing one */
    public SubscribeResults updateSubscription(String url) throws IOException {
        Feed feed = feedFactory.fromUrl(url);
        return subscribe(feed, showsDb, episodesDb);
    }
}
