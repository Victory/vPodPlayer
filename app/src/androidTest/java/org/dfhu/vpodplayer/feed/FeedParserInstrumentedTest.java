package org.dfhu.vpodplayer.feed;

import android.support.test.runner.AndroidJUnit4;


import junit.framework.Assert;

import org.dfhu.vpodplayer.model.Episode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.DataFormatException;


@RunWith(AndroidJUnit4.class)
public class FeedParserInstrumentedTest extends Assert {

    @Test
    public void planetMoney() throws IOException, DataFormatException, XmlPullParserException {
        InputStream in = getPlanetMoneyInputStream();

        Document doc = Jsoup.parse(in, "UTF-8", "");
        String url = "http://example.com/feed.xml";
        Feed feed = new JsoupFeed(url, doc);
        assertEquals("Planet Money", feed.getTitle());

        List<Episode> items = feed.getEpisodes();
        assertTrue(items.size() == 2);

        String expected = "http://play.podtrac.com/npr-510289/npr.mc.tritondigital.com/NPR_510289/media/anon.npr-mp3/npr/pmoney/2016/09/20160901_pmoney_pmpod.mp3?orgId=1&d=1092&p=510289&story=492283098&t=podcast&e=492283098&ft=pod&f=510289";
        String actual = items.get(0).url;
        assertEquals(expected, actual);
    }

    public InputStream getPlanetMoneyInputStream() {
        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss xmlns:npr=\"http://www.npr.org/rss/\" xmlns:nprml=\"http://api.npr.org/nprml\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" version=\"2.0\">\n" +
                "  <channel>\n" +
                "    <title>Planet Money</title>\n" +
                "    <link>http://www.npr.org/planetmoney</link>\n" +
                "    <description><![CDATA[The economy, explained, with stories and surprises. Imagine you could call up a friend and say, \"Meet me at the bar and tell me what's going on with the economy.\" Now imagine that's actually a fun evening. That's what we're going for at <em>Planet Money</em>. People seem to like it.]]></description>\n" +
                "    <copyright>2015 National Public Radio</copyright>\n" +
                "    <generator>NPR API RSS Generator 0.94</generator>\n" +
                "    <language>en</language>\n" +
                "    <itunes:summary><![CDATA[The economy, explained, with stories and surprises. Imagine you could call up a friend and say, \"Meet me at the bar and tell me what's going on with the economy.\" Now imagine that's actually a fun evening. That's what we're going for at <em>Planet Money</em>. People seem to like it.]]></itunes:summary>\n" +
                "    <itunes:subtitle><![CDATA[The economy, explained, with stories and surprises. Imagine you could call up a friend and say, \"Meet me at the bar and tell me what's going on with the economy.\" Now imagine that's actually a fun evening. That's what we're going for at <em>Planet Money</em>. People seem to like it.]]></itunes:subtitle>\n" +
                "    <itunes:author>NPR</itunes:author>\n" +
                "    <itunes:block>no</itunes:block>\n" +
                "    <itunes:owner>\n" +
                "      <itunes:email>podcasts@npr.org</itunes:email>\n" +
                "      <itunes:name>NPR</itunes:name>\n" +
                "    </itunes:owner>\n" +
                "    <itunes:category text=\"Business\"/>\n" +
                "    <itunes:category text=\"News &amp; Politics\"/>\n" +
                "    <itunes:category text=\"Business News\"/>\n" +
                "    <itunes:image href=\"https://media.npr.org/assets/img/2015/12/18/planetmoney_sq-c7d1c6f957f3b7f701f8e1d5546695cebd523720.jpg?s=1400\"/>\n" +
                "    <image>\n" +
                "      <url>https://media.npr.org/assets/img/2015/12/18/planetmoney_sq-c7d1c6f957f3b7f701f8e1d5546695cebd523720.jpg?s=200</url>\n" +
                "      <title>Planet Money</title>\n" +
                "      <link>http://www.npr.org/planetmoney</link>\n" +
                "    </image>\n" +
                "    <lastBuildDate>Fri, 02 Sep 2016 13:07:00 -0400</lastBuildDate>\n" +
                "    <item>\n" +
                "      <title>#532: The Wild West of the Internet</title>\n" +
                "      <description><![CDATA[For decades, most websites ended in either .com, .net, or .org. But a few years ago, everything changed.]]></description>\n" +
                "      <pubDate>Fri, 02 Sep 2016 13:07:00 -0400</pubDate>\n" +
                "      <copyright>2015 National Public Radio</copyright>\n" +
                "      <guid>7a41ffd5-862d-43a7-9405-e8524a11d68d</guid>\n" +
                "      <itunes:author>NPR</itunes:author>\n" +
                "      <itunes:summary><![CDATA[For decades, most websites ended in either .com, .net, or .org. But a few years ago, everything changed.]]></itunes:summary>\n" +
                "      <itunes:subtitle><![CDATA[For decades, most websites ended in either .com, .net, or .org. But a few years ago, everything changed.]]></itunes:subtitle>\n" +
                "      <itunes:image href=\"https://media.npr.org/assets/img/2016/09/01/wild-west-of-the-internet-image_wide-cc1b9f0aff789e638be696987f5a03c9af46c84b.png?s=1400\"/>\n" +
                "      <itunes:duration>1092</itunes:duration>\n" +
                "      <itunes:explicit>no</itunes:explicit>\n" +
                "      <enclosure url=\"http://play.podtrac.com/npr-510289/npr.mc.tritondigital.com/NPR_510289/media/anon.npr-mp3/npr/pmoney/2016/09/20160901_pmoney_pmpod.mp3?orgId=1&amp;d=1092&amp;p=510289&amp;story=492283098&amp;t=podcast&amp;e=492283098&amp;ft=pod&amp;f=510289\" length=\"0\" type=\"audio/mpeg\"/>\n" +
                "    </item>" +
                "    <item>\n" +
                "      <title>#722: The New Telenovela</title>\n" +
                "      <description><![CDATA[One telenovela actress-turned-executive decided to write a new kind of drama. Her show changed the landscape of Spanish language TV--and of all TV.]]></description>\n" +
                "      <pubDate>Wed, 31 Aug 2016 17:40:00 -0400</pubDate>\n" +
                "      <copyright>2015 National Public Radio</copyright>\n" +
                "      <guid>e077248b-47ca-451a-b538-001e040930d4</guid>\n" +
                "      <itunes:author>NPR</itunes:author>\n" +
                "      <itunes:summary><![CDATA[One telenovela actress-turned-executive decided to write a new kind of drama. Her show changed the landscape of Spanish language TV--and of all TV.]]></itunes:summary>\n" +
                "      <itunes:subtitle><![CDATA[One telenovela actress-turned-executive decided to write a new kind of drama. Her show changed the landscape of Spanish language TV--and of all TV.]]></itunes:subtitle>\n" +
                "      <itunes:image href=\"https://media.npr.org/assets/img/2016/08/31/gettyimages-504836746_wide-5bc4597fe7b65f9f833b0b7a58fabef83078f4db.jpg?s=1400\"/>\n" +
                "      <itunes:duration>1232</itunes:duration>\n" +
                "      <itunes:explicit>yes</itunes:explicit>\n" +
                "      <enclosure url=\"http://play.podtrac.com/npr-510289/npr.mc.tritondigital.com/NPR_510289/media/anon.npr-mp3/npr/pmoney/2016/08/20160831_pmoney_podcast083116.mp3?orgId=1&amp;d=1232&amp;p=510289&amp;story=492123023&amp;t=podcast&amp;e=492123023&amp;ft=pod&amp;f=510289\" length=\"0\" type=\"audio/mpeg\"/>\n" +
                "    </item>" +
                "  </channel>" +
                "</rss>";

        return new ByteArrayInputStream(text.getBytes());
    }
}
