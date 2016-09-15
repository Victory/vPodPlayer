package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.dfhu.vpodplayer.feed.Feed;
import org.dfhu.vpodplayer.feed.FetchFeed;
import org.dfhu.vpodplayer.feed.FeedFetchResult;
import org.dfhu.vpodplayer.feed.JsFeed;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;

public class Podcasts extends AppCompatActivity {

    private static final String sTestFeed = "http://www.npr.org/rss/podcast.php?id=510289";

    @BindView(R.id.testTitle)
    TextView testTitle;
    private static Observable<Feed> fetchObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        ButterKnife.bind(this);

        Log.d("test-title", "on create: " + testTitle.toString());

        if (fetchObserver != null) {
            Log.d("test-title", "re-subing");
            fetchObserver.subscribe(new FetchSubscriber());
        } else {

            Log.d("test-title", "fetchObserver is null");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fetchObserver != null) {
            Log.d("test-title", "un-subscribing");
            // XXX: this doesn't un-subscribe as expected, turning the screen will still run the onNext multiple times
            fetchObserver.unsubscribeOn(AndroidSchedulers.mainThread());
        }
    }

    private class FetchSubscriber extends Subscriber<Feed> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            setTitle("Their was an error parsing the feed " + e.getMessage());
        }

        @Override
        public void onNext(Feed feed) {
            String title = feed.getTitle();
            Log.d("test-title", "onNext: " + testTitle.toString());
            testTitle.setText(title);
        }
    }

    @OnClick(R.id.testFetchFeed)
    public void fetchFeed() {

        Log.d("test-title", "onclick: " + testTitle.toString());
        final Context theContext = (Context) this;

        final VicURL url;
        try {
            url = VicURLProvider.newInstance(sTestFeed);
        } catch (MalformedURLException e) {
            // XXX: replace with material error
            Toast.makeText(theContext, "bad url", Toast.LENGTH_SHORT);
            return;
        }

        fetchObserver = Observable.defer(
                new Func0<Observable<FeedFetchResult>>() {
                    @Override
                    public Observable<FeedFetchResult> call() {
                        return FetchFeed.fetch(url);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<FeedFetchResult, Observable<Feed>>() {
                    @Override
                    public Observable<Feed> call(FeedFetchResult feedFetchResult) {
                        if (feedFetchResult.status == FeedFetchResult.Status.SUCCESS) {
                            Document doc = null;
                            try {
                                doc = Jsoup.parse(feedFetchResult.inputStream, "UTF-8", "");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                Thread.sleep(8000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Feed feed = new JsFeed(doc);

                            return Observable.just(feed);
                        }

                        Log.e("feed-fetch", "success not returned: " + feedFetchResult.status.toString());
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
                fetchObserver.subscribe(new FetchSubscriber());

        Log.d("testing", "started download and parse");
    }

    private void setTitle(String title) {
        testTitle = (TextView) findViewById(R.id.testTitle);
        testTitle.setText(title);
    }
}
