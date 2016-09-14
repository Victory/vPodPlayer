package org.dfhu.vpodplayer.feed;


import android.util.Log;

import org.dfhu.vpodplayer.util.LogTags;

public class FeedParser {

    public static final String ITEM_ID_VERSION_PREFIX = "V1-";

    public static FeedParserResult parse(Feed feed) {
        FeedInfo feedInfo = new FeedInfo();

        feedInfo.setTitle(feed.getTitle());
        for (FeedItem item: feed.getItems()) {
            EpisodeInfo episode = new EpisodeInfo();
            try {
                episode.setLink(item.getLink());
                episode.setId(getItemId(item));
            } catch (NoLinkException e) {
                Log.e(LogTags.PARSE_FEED, "Could not parse: " + item.getTitle());
            }
            episode.setTitle(item.getTitle());
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
