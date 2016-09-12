package org.dfhu.vpodplayer.feed;


import android.util.Log;

import org.dfhu.vpodplayer.util.LogTags;

public class FeedParser {

    public static final int MAX_ITEMS = 80;
    public static final String ITEM_ID_VERSION_PREFIX = "V1-";

    public static FeedParserResult parse(Feed feed) {
        FeedInfo feedInfo = new FeedInfo();

        feedInfo.setTitle(feed.getTitle());
        for (FeedItem item: feed.getItems()) {
            EpisodeInfo episode = new EpisodeInfo();
            try {
                String link = getLink(item);
                episode.setLink(link);
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
        sb.append(getLink(item));
        return sb.toString();
    }

    private static String getLink(FeedItem item) throws NoLinkException {
        for (FeedItemEnclosure enclosure: item.getEnclosures()) {
            if (!enclosure.getType().startsWith("audio")) {
                continue;
            }
            return enclosure.getLink();
        }

        throw new NoLinkException();
    }

    private static class NoLinkException extends Exception {}


}
