package org.dfhu.vpodplayer.feed;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

public class EarlProvider {
    /**
     * Get the Feed and status of Earl.parseOrThrow
     */
    static EarlResult getEarl(InputStream inputStream) {
        EarlResult result = new EarlResult();
        result.status = FeedParserResult.Status.SUCCESS;
        try {
            result.feed = EarlParser.parseOrThrow(inputStream, FeedParser.MAX_ITEMS);
        } catch (XmlPullParserException e) {
            result.status = FeedParserResult.Status.BAD_XML_FORMAT;
            result.e = e;
        } catch (IOException e) {
            result.status = FeedParserResult.Status.COULD_NOT_READ_XML;
            result.e = e;
        } catch (DataFormatException e) {
            result.status = FeedParserResult.Status.COULD_NOT_PARSE_XML;
            result.e = e;
        }
        return result;
    }

    public static class EarlResult {
        public Feed feed;
        public FeedParserResult.Status status;
        public Exception e;
    }
}
