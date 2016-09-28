package org.dfhu.vpodplayer.feed;

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

    @Override
    public List<FeedItem> getItems() {
        Elements elms = doc.select("rss > channel item");
        List<FeedItem> items = new ArrayList<FeedItem>();

        for (Element elm: elms) {
            FeedItem item = new JsoupFeedItem(elm);
            items.add(item);
        }

        return items;
    }
}
