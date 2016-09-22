package org.dfhu.vpodplayer;

import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.dfhu.vpodplayer.feed.Feed;
import org.dfhu.vpodplayer.feed.FeedFetchResult;
import org.dfhu.vpodplayer.feed.FeedParser;
import org.dfhu.vpodplayer.feed.FeedParserResult;
import org.dfhu.vpodplayer.feed.FetchFeed;
import org.dfhu.vpodplayer.feed.JsFeed;
import org.dfhu.vpodplayer.tasks.FetchFeedFragment;
import org.dfhu.vpodplayer.util.VicURL;
import org.dfhu.vpodplayer.util.VicURLProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class Podcasts extends AppCompatActivity
        implements FetchFeedFragment.FetchFeedCallbacks, FeedFetcher {

    //private static final String sTestFeed = "http://www.npr.org/rss/podcast.php?id=510289";

    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    @BindView(R.id.testTitle)
    TextView testTitle;

    private FetchFeedFragment mFetchFeedFragment;
    private static final String TAG_FETCH_FEED_FRAGMENT = "fetch-feed-fragment";
    private static final CompositeSubscription subs = new CompositeSubscription();

    private final Bundle configChangeBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcasts);
        ButterKnife.bind(this);
        Log.d("test-title", "on create: " + testTitle.toString());
        setSupportActionBar(toolbar);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFetchFeedFragment != null) {
            mFetchFeedFragment.getArguments().remove(BUNDLE_KEY_ADD_SUBSCRIPTION);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        subs.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.main_menu, menu);

        bindSubscribeMenuItem(menu);

        return true;
    }

    /** set the binding for subscribe ActionView */
    private void bindSubscribeMenuItem(Menu menu) {
        MenuItem subscribeItem = (MenuItem) menu.findItem(R.id.menu_subscribe);
        final SubscribeActionView subscribeView = (SubscribeActionView) MenuItemCompat.getActionView(subscribeItem);
        subscribeView.setFeedFetcher(this);

        MenuItemCompat.setOnActionExpandListener(subscribeItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                TextView textView = (TextView) findViewById(R.id.subscribe_url);
                textView.setText("");
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh_podcast:
                Toast.makeText(this, "not implemented", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void addFetchFeedSubscription(Observable<VicURL> observable) {
        subs.clear();

        Subscription evt;
        evt = observable
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<VicURL, Observable<InputStream>>() {
                    @Override
                    public Observable<InputStream> call(VicURL vicURL) {
                        Log.d("test-title", "fetching inputstream: " + vicURL.getUrlString());
                        try {
                            return Observable.just(FetchFeed.getInputStreamSync(vicURL));
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    }
                })
                .flatMap(new Func1<InputStream, Observable<Feed>>() {
                    @Override
                    public Observable<Feed> call(InputStream inputStream) {
                        try {
                            Document doc = Jsoup.parse(inputStream, "UTF-8", "");
                            JsFeed feed = new JsFeed(doc);
                            return Observable.just((Feed) feed);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feed>() {
                    @Override
                    public void onCompleted() {
                        Log.d("test-title", "onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("test-title", "error: " + e.getMessage());
                        toasty(e.getMessage());
                    }

                    @Override
                    public void onNext(Feed r) {
                        Log.d("test-title", "onNext");
                        setTitle(r.getTitle());
                    }
                });
        subs.add(evt);
    }

    private void toasty(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFetchFeed(String feedUrl) {

        // prefix with http
        if (!feedUrl.startsWith("http://")) {
            feedUrl = "http://" + feedUrl;
        }

        mFetchFeedFragment = new FetchFeedFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_PODCAST_URL, feedUrl);
        args.putBoolean(BUNDLE_KEY_ADD_SUBSCRIPTION, true);
        mFetchFeedFragment.setArguments(args);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(mFetchFeedFragment, TAG_FETCH_FEED_FRAGMENT).commit();
    }

    private void setTitle(String title) {
        configChangeBundle.putString("title", title);
        testTitle.setText(title);
    }
}
