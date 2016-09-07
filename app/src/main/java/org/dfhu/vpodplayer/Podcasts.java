package org.dfhu.vpodplayer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.dfhu.vpodplayer.feed.FetchFeed;
import org.dfhu.vpodplayer.feed.FetchFeed.FeedResult;

public class Podcasts extends AppCompatActivity {

    Button mFetchFeeds;

    private static final String sTestFeed = "http://www.npr.org/rss/podcast.php?id=510289";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        mFetchFeeds = (Button) findViewById(R.id.testFetchFeed);

        mFetchFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FetchAFeed faf = new FetchAFeed();
                faf.execute(sTestFeed);

                Log.d("testing", "button was clicked 3");
            }
        });
    }

    private class FetchAFeed extends AsyncTask<String, Integer, FeedResult> {

        @Override
        protected FeedResult doInBackground(String... feedUrls) {
            FeedResult result = FetchFeed.fetch(feedUrls[0]);
            return result;
        }
    }
}
