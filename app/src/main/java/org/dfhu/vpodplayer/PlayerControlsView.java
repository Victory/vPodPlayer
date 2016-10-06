package org.dfhu.vpodplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;


public class PlayerControlsView extends View {

    private static final String LOW_COLOR = "#558899";
    private static final String INNER_COLOR = "#990099";
    private final Paint outterPaint;
    private final Paint innerPaint;
    private float size;
    private float centerX;
    private float centerY;
    private float width;
    private float height;


    public PlayerControlsView(Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        outterPaint = new Paint();
        outterPaint.setColor(Color.parseColor(LOW_COLOR));
        outterPaint.setAntiAlias(true);

        innerPaint = new Paint();
        innerPaint.setColor(Color.parseColor(INNER_COLOR));
        innerPaint.setAntiAlias(true);

        ViewTreeObserver observer = getViewTreeObserver();

        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                width = getMeasuredWidth();
                height = getMeasuredHeight();

                if (width > height) {
                    size = height / 2;
                } else {
                    size = width / 2;
                }
                size -= 20;
                centerX = width / 2;
                centerY = height / 2;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, size, outterPaint);
        canvas.drawCircle(centerX, centerY, size / 4, innerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        logMotionEvent(event);
        super.onTouchEvent(event);
        return true;
    }

    @NonNull
    private void logMotionEvent(MotionEvent event) {
        String actionString;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionString = "down";
                handleCenterClick(x, y, event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionString = "move";
                break;
            case MotionEvent.ACTION_UP:
                actionString = "up";
                break;
            default:
                actionString = "unknown";
        }
        Log.d("touch-event", x + "X" + y + " - " + actionString);
    }

    public void handleCenterClick(float x, float y, MotionEvent event) {
        if (onCenterClickListener == null) {
            return;
        }

        float cfx = x - centerX;
        float cfy = y - centerY;
        double z = Math.sqrt(cfx*cfx + cfy*cfy);
        if (z > size / 4) {
            return;
        }
        onCenterClickListener.click(event);


    }
    private OnCenterClickListener onCenterClickListener;
    public static interface OnCenterClickListener {
       void click(MotionEvent event);
    }

    public void setOnCenterClickListener(OnCenterClickListener listener) {
        onCenterClickListener = listener;
    }
}
