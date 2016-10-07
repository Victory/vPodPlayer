package org.dfhu.vpodplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;


public class PlayerControlsView extends View {

    public static final String LOW_COLOR = "#558899";
    public static final String INNER_COLOR_PLAY = "#AA33AA";
    public static final String INNER_COLOR_PAUSE = "#660066";
    public static final String LISTENED_ARC_COLOR = "#00FF00";
    public static final String DEBUG_COLOR = "#FF0000";
    private final Paint outerPaint;
    private final Paint innerPaint;
    private final Paint listenArcPaint;
    private final Paint debugPaint;
    private float size;
    private float centerX;
    private float centerY;
    private float width;
    private float height;
    double arcLength = 1;

    private final PlayPositionHandler playPositionHandler;

    RectF arcRect;

    public PlayerControlsView(Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        outerPaint = new Paint();
        outerPaint.setColor(Color.parseColor(LOW_COLOR));
        outerPaint.setAntiAlias(true);

        innerPaint = new Paint();
        innerPaint.setColor(Color.parseColor(INNER_COLOR_PLAY));
        innerPaint.setAntiAlias(true);


        listenArcPaint = new Paint();
        listenArcPaint.setColor(Color.parseColor(LISTENED_ARC_COLOR));
        listenArcPaint.setAntiAlias(true);

        debugPaint = new Paint();
        debugPaint.setColor(Color.parseColor(DEBUG_COLOR));
        debugPaint.setAntiAlias(true);

        playPositionHandler = new PlayPositionHandler();

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
        canvas.drawCircle(centerX, centerY, size, outerPaint);

        float top = centerY - size;
        float bottom = centerY + size;
        float left = centerX - size;
        float right = centerX + size;
        arcRect = new RectF(left, top, right, bottom);
        canvas.drawArc(arcRect, 270, (float) arcLength, true, listenArcPaint);
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

        float x = event.getX();
        float y = event.getY();

        boolean isCenterAction = isCenterAction(x, y);

        if (!isCenterAction) {
            playPositionHandler.handle(x, y, event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isCenterAction) {
                    handleCenterClick(x, y, event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        //Log.d("touch-event", x + "X" + y + " percent " + percent + " - " + actionString);
    }

    public void setCenterColor(String hexColor) {
        innerPaint.setColor(Color.parseColor(hexColor));
        invalidate();
    }

    public void updatePlayer(double positionPercent) {
        Log.d("PlayerControlsView", "updatePlayer: " + positionPercent);
        double deg = 360 * positionPercent;
        arcLength = deg;
        invalidate();
    }

    private class PlayPositionHandler {
        double deg = 0;
        double lastDeg = 0;
        double downDeg;

        public void handle(float x, float y, MotionEvent event) {

            float cfx = x - centerX;
            float cfy = y - centerY;
            double rads = Math.atan2(cfy, cfx) + 360;
            deg = Math.toDegrees(rads) % 360;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downDeg = deg;
                    lastDeg = deg;
                    break;
                case MotionEvent.ACTION_MOVE:
                    double mdd = deg - lastDeg;

                    if (Math.abs(mdd) > 300) {
                        //Log.d("motion-event", "DISCONTINUOUS");
                        mdd = 0;
                    }
                    arcLength += mdd;

                    if (arcLength + mdd >= 360) {
                        arcLength = 360;
                        //Log.d("motion-event", "TOO BIG");
                    } else if (arcLength + mdd <= 0) {
                        arcLength = 0;
                        //Log.d("motion-event", "TOO SMALL");
                    } else {
                        arcLength += mdd;
                    }
                    //Log.d("move-event", "mdd: " + mdd + " deg: " + deg + " lastDeg:" + lastDeg);
                    lastDeg = deg;
                    invalidate();
                    double percent = (Math.toDegrees(rads) % 360) / 360;
                    if (onPositionListener != null) {
                        onPositionListener.positionChange(percent);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }

        }
    }

    public boolean isCenterAction (float x, float y) {
        float cfx = x - centerX;
        float cfy = y - centerY;
        double z = Math.sqrt(cfx*cfx + cfy*cfy);
        if (z > size / 4) {
            return false;
        }
        return true;
    }

    public boolean handleCenterClick(float x, float y, MotionEvent event) {
        if (onCenterClickListener == null) {
            return false;
        }

        onCenterClickListener.click(this);
        return true;
    }

    private OnCenterClickListener onCenterClickListener;
    public interface OnCenterClickListener {
       void click(PlayerControlsView view);
    }
    public void setOnCenterClickListener(OnCenterClickListener listener) {
        onCenterClickListener = listener;
    }

    private OnPositionListener onPositionListener;
    public interface OnPositionListener {
        /**
         * Triggered on MotionEvent.ACTION_UP of position rotation
         *
         * @param positionPercent - the perecent of arc filled
         */
        void positionChange(double positionPercent);
    }
    public void setOnPositionListener(OnPositionListener listener) {
        onPositionListener = listener;
    }
}
