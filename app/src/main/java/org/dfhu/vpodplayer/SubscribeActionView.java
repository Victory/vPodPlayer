package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.view.CollapsibleActionView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


@SuppressWarnings("unused") // is used in XML
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

        ContextMenu.ContextMenuInfo contextMenuInfo = this.getContextMenuInfo();

        final TextView subscribeUrl = (TextView) findViewById(R.id.subscribe_url);
        subscribeUrl.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    VPodPlayer.CloseSubscribeActionViewBus.publish();

                    // Close the open soft keyboard
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                    String showUrl = subscribeUrl.getText().toString();
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    Context applicationContext = getContext().getApplicationContext();
                    VPodPlayer.startSubscribeService(applicationContext, showUrl);
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
