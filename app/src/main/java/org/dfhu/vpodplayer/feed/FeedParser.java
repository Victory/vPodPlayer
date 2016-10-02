package org.dfhu.vpodplayer.feed;


import android.util.Log;

import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.util.LogTags;

public class FeedParser {

    public static final String ITEM_ID_VERSION_PREFIX = "V1-";

    public static FeedParserResult parse(Feed feed) {
        FeedInfo feedInfo = new FeedInfo();

        feedInfo.setTitle(feed.getTitle());
        for (Episode ep: feed.getEpisodes()) {
            EpisodeInfo episode = new EpisodeInfo();
            episode.setLink(ep.url);
            episode.setTitle(ep.title);
            feedInfo.addEpisode(episode);
        }

        return new FeedParserResult(FeedParserResult.Status.SUCCESS, feedInfo);
    }

    /** Set a unique Id, use Version prefix for future proofing as poorly written feeds are discovered */
    private static String getItemId(FeedItem item) throws NoLinkException {
        StringBuilder sb = new StringBuilder();
        sb.append(ITEM_ID_VERSION_PREFIX);
        sb.append(item.getLink());
        return sb.toString();
    }


    private static class NoLinkException extends Exception {}


}
