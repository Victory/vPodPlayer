package org.dfhu.vpodplayer.feed;

import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class FeedFactory {

    public Feed fromUrl(String url) throws IOException {
        InputStream inputStream = createInputStream(url);
        return fromInputStream(url, inputStream);
    }

    public Feed fromInputStream(String url, InputStream inputStream) throws IOException {
        if (url == null) throw new NullPointerException("url can't be null");
        Document doc = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
        return new JsoupFeed(url, doc);
    }

    private InputStream createInputStream(String url) throws IOException {
        VicURL vicURL = VicURLProvider.newInstance(url);
        URLConnection connection = vicURL.openConnection();
        return new BufferedInputStream(connection.getInputStream());
    }
}
