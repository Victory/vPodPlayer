package org.dfhu.vpodplayer.feed;


import org.dfhu.vpodplayer.model.Episode;

class FeedParser {

    private static final String ITEM_ID_VERSION_PREFIX = "V1-";

    static FeedParserResult parse(Feed feed) {
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
        return ITEM_ID_VERSION_PREFIX + item.getUrl();
    }


    private static class NoLinkException extends Exception {}


}
