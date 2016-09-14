package org.dfhu.vpodplayer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.dfhu.vpodplayer.feed.FetchFeed;
import org.dfhu.vpodplayer.feed.FeedFetchResult;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;

import java.net.MalformedURLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Podcasts extends AppCompatActivity {

    @BindView(R.id.testFetchFeed)
    Button mFetchFeeds;

    private static final String sTestFeed = "http://www.npr.org/rss/podcast.php?id=510289";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        ButterKnife.bind(this);

        mFetchFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FetchAFeed faf = new FetchAFeed();
                faf.execute(sTestFeed);

                Log.d("testing", "button was clicked 3");
            }
        });
    }

    private class FetchAFeed extends AsyncTask<String, Integer, FeedFetchResult> {

        @Override
        protected FeedFetchResult doInBackground(String... feedUrls) {

            // XXX: should not pass string, instead build URL in caller
            try {
                VicURL url = VicURLProvider.newInstance(feedUrls[0]);
                return FetchFeed.fetch(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
