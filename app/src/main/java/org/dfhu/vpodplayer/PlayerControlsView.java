package org.dfhu.vpodplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.ArcShape;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;


public class PlayerControlsView extends View {

    private static final String LOW_COLOR = "#558899";
    private static final String INNER_COLOR = "#990099";
    private static final String LISTENED_ARC_COLOR = "#00FF00";
    private static final String DEBUG_COLOR = "#FF0000";
    private final Paint outterPaint;
    private final Paint innerPaint;
    private final Paint listenArcPaint;
    private final Paint debugPaint;
    private float size;
    private float centerX;
    private float centerY;
    private float width;
    private float height;
    double deg = 1;
    double downDeg;
    double lastDeg;
    boolean rotateLock = false;

    RectF arcRect;

    public PlayerControlsView(Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        outterPaint = new Paint();
        outterPaint.setColor(Color.parseColor(LOW_COLOR));
        outterPaint.setAntiAlias(true);

        innerPaint = new Paint();
        innerPaint.setColor(Color.parseColor(INNER_COLOR));
        innerPaint.setAntiAlias(true);


        listenArcPaint = new Paint();
        listenArcPaint.setColor(Color.parseColor(LISTENED_ARC_COLOR));
        listenArcPaint.setAntiAlias(true);

        debugPaint = new Paint();
        debugPaint.setColor(Color.parseColor(DEBUG_COLOR));
        debugPaint.setAntiAlias(true);

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

        float top = centerY - size;
        float bottom = centerY + size;
        float left = centerX - size;
        float right = centerX + size;
        arcRect = new RectF(left, top, right, bottom);
        canvas.drawArc(arcRect, 270, (float) deg, true, listenArcPaint);

        canvas.drawCircle(centerX, centerY, size / 4, innerPaint);

        //canvas.drawRect(left, top, right, bottom, debugPaint);
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

        float cfx = x - centerX;
        float cfy = y - centerY;
        double rads = Math.atan2(cfy, cfx) + 360;
        deg = Math.toDegrees(rads) % 360;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionString = "down";
                handleCenterClick(x, y, event);
                downDeg = deg;
                break;
            case MotionEvent.ACTION_MOVE:
                actionString = "move";
                if (downDeg < deg) {
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                actionString = "up";
                break;
            default:
                actionString = "unknown";
        }
        lastDeg = deg;

        double percent = 100 * (Math.toDegrees(rads) % 360) / 360;
        Log.d("touch-event", x + "X" + y + " percent " + percent + " - " + actionString);
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
