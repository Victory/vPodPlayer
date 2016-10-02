package org.dfhu.vpodplayer.feed;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class JsoupFeedItem implements FeedItem {

    private final Element elm;

    JsoupFeedItem(Element elm) {
        this.elm = elm;
    }

    @Override
    public String getTitle(){
        return elm.select("title").text();
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
