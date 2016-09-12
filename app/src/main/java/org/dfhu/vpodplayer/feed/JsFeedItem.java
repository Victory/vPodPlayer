package org.dfhu.vpodplayer.feed;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class JsFeedItem implements FeedItem {

    private final Element elm;

    public JsFeedItem(Element elm) {
        this.elm = elm;
    }
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getLink() {
        Elements elms = elm.select("enclosure");
        String type;
        for(Element elm: elms) {
            type = elm.attr("type");
            if (type == null || !type.startsWith("audio")) {
                continue;
            }
            return elm.attr("url");
        }

        return "";
    }
}
