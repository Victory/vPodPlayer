package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.view.CollapsibleActionView;
import android.support.v7.widget.LinearLayoutCompat;


public class SubscribeActionView extends LinearLayoutCompat implements CollapsibleActionView {
    public SubscribeActionView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.subscribe_action, this);
    }

    @Override
    public void onActionViewExpanded() {

    }

    @Override
    public void onActionViewCollapsed() {

    }
}
