package org.dfhu.vpodplayer.feed;


import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class FetchFeed {

    public static class FeedResult {

        enum Status {
            MALFORMED_URL,
            COULD_NOT_OPEN_URL,
            COULD_NOT_READ_STREAM,
            SUCCESS
        }

        public final String data;
        public final Status status;
        public final Exception exception;

        /** Set the status, leave data empty and store the exception */
        public FeedResult(Status status, IOException e) {
            this.status = status;
            this.exception = e;
            this.data = "";
            Log.w("Could not fetch feed", e);
        }

        /** Set the data. Status is SUCCESS and exception is null */
        public FeedResult(String data) {
            this.status = Status.SUCCESS;
            this.data = data;
            this.exception = null;
        }
    }

    /**
     * Gets the feed data. Assumes that the rss feed is encoded with UTF-8
     *
     * @param feedURL - full url with http(s) prefix
     * @return has information about the attempted feed retrieval
     */
    public static FeedResult fetch(String feedURL) {
        URL url;
        try {
            url = new URL(feedURL);
        } catch (MalformedURLException e) {
            return new FeedResult(FeedResult.Status.MALFORMED_URL, e);
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return new FeedResult(FeedResult.Status.COULD_NOT_OPEN_URL, e);
        }

        String data;
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
            data = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new FeedResult(FeedResult.Status.COULD_NOT_READ_STREAM, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return new FeedResult(data);
    }
}
