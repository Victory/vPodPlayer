package org.dfhu.vpodplayer.feed;


import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.dfhu.vpodplayer.VPodPlayer.safeToast;

public class SubscribeToFeed {

    private final Shows showsDb;
    private final Episodes episodesDb;
    public SubscribeToFeed(Shows showsDb, Episodes episodesDb) {
        this.showsDb = showsDb;
        this.episodesDb = episodesDb;
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

    public void fetchNew(String url) throws IOException {
        VicURL vicURL = VicURLProvider.newInstance(url);
        InputStream inputStream = FetchFeed.getInputStreamSync(vicURL);
        Document doc = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
        JsoupFeed feed = new JsoupFeed(url, doc);
        SubscribeToFeed.subscribe(feed, showsDb, episodesDb);
    }
}
