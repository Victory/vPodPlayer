package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.view.CollapsibleActionView;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


public class SubscribeActionView extends LinearLayoutCompat implements CollapsibleActionView {

    FeedFetcher feedFetcher;

    public SubscribeActionView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.subscribe_action, this);
    }

    public void setFeedFetcher(FeedFetcher feedFetcher) {
        this.feedFetcher = feedFetcher;
    }

    @Override
    public void onActionViewExpanded() {

        final TextView subscribeUrl = (TextView) findViewById(R.id.subscribe_url);
        subscribeUrl.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

                    // Close the open soft keyboard
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    feedFetcher.triggerFetchFeed(subscribeUrl.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActionViewCollapsed() {

    }
}
