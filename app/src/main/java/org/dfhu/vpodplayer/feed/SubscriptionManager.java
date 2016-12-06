package org.dfhu.vpodplayer.feed;


import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.FeedFetcher;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import static org.dfhu.vpodplayer.VPodPlayer.safeToast;

public class SubscriptionManager {

    private final Shows showsDb;
    private final Episodes episodesDb;
    private final FeedFactory feedFactory;

    private SubscriptionManager(FeedFactory feedFactory, Shows showsDb, Episodes episodesDb) {
        this.showsDb = showsDb;
        this.episodesDb = episodesDb;
        this.feedFactory = feedFactory;
    }

    public static class Builder {
        private Shows showsDb;
        private Episodes episodesDb;
        private FeedFactory feedFactory;

        public Builder showsDb(@NonNull Shows showsDb) {
            this.showsDb = showsDb;
            return this;
        }

        public Builder episodesDb(@NonNull Episodes episodesDb) {
            this.episodesDb = episodesDb;
            return this;
        }

        public Builder feedFactory(@NonNull FeedFactory feedFactory) {
            this.feedFactory = feedFactory;
            return this;
        }

        public SubscriptionManager build() {
            return new SubscriptionManager(this.feedFactory, this.showsDb, this.episodesDb);
        }

    }

    /** Store show and episodes from the feed in the database */
    @Deprecated
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

    /** Get a brand new feed */
    public Show subscribeToFeed(String url) throws IOException {
        Feed feed = feedFactory.fromUrl(url);
        return SubscriptionManager.subscribe(feed, showsDb, episodesDb);
    }

    /** Get list of new episodes */
    public void refreshFeed(String url) throws IOException {
        Feed feed = feedFactory.fromUrl(url);
        SubscriptionManager.subscribe(feed, showsDb, episodesDb);
    }
}
