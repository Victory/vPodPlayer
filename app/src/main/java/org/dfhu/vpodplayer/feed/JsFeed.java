package org.dfhu.vpodplayer.feed;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class JsFeed implements Feed {

    private Document doc;

    public JsFeed(Document doc) {
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
    public List<FeedItem> getItems() {
        Elements elms = doc.select("rss > channel item");
        List<FeedItem> items = new ArrayList<FeedItem>();

        for (Element elm: elms) {
            FeedItem item = new JsFeedItem(elm);
            items.add(item);
        }

        return items;
    }
}
