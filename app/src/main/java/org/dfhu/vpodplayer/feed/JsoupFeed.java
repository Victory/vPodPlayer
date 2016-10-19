package org.dfhu.vpodplayer.feed;

import org.dfhu.vpodplayer.model.Episode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class JsoupFeed implements Feed {

    private final Document doc;
    private final String url;

    public JsoupFeed(String url, Document doc) {
        this.url = url;
        this.doc = doc;
    }

    @Override
    public String getTitle() {
        Elements found = doc.select("rss > channel > title");
        if (found.size() == 0) {
           return "Unknown Title";
        }

        Element elm = found.get(0);
        return elm.text();
    }

    @Override
    public String getId() {
        return "V1-" + getUrl().toLowerCase().replaceAll("[^a-zA-Z0-9]", "-");
    }

    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Get episode info, the consumer is expected to set the showId before inserting
     * @return - episodes, without the showId
     */
    @Override
    public List<Episode> getEpisodes() {
        Elements elms = doc.select("rss > channel item");
        List<Episode> items = new ArrayList<>();

        for (Element elm: elms) {
            FeedItem item = new JsoupFeedItem(elm);
            Episode episode = new Episode();
            episode.url = item.getUrl();
            episode.title = item.getTitle();
            episode.pubDate = item.getPubDate();
            items.add(episode);
        }

        return items;
    }
}
