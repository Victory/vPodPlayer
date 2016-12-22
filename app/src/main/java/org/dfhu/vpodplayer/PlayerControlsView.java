package org.dfhu.vpodplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import org.dfhu.vpodplayer.util.ColorResource;

import javax.inject.Inject;


public class PlayerControlsView extends View {

    @Inject
    ColorResource colorResource;

    final Paint outerPaint;
    final Paint innerPaint;
    final Paint listenArcPaint;
    final Paint textPaint;
    float size;
    float centerX;
    float centerY;
    float width;
    float height;
    double arcLength = 1;
    PlayerInfo playerInfo = new PlayerInfo();

    boolean isMoving = false;

    final PlayPositionHandler playPositionHandler;

    RectF arcRect = new RectF();

    public PlayerControlsView(Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
        ((VPodPlayerApplication) context.getApplicationContext()).component().inject(this);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(80);
        textPaint.setColor(colorResource.get(R.color.colorDownloaded));

        outerPaint = new Paint();
        outerPaint.setColor(colorResource.get(R.color.colorPartiallyListened));
        outerPaint.setAntiAlias(true);

        innerPaint = new Paint();
        innerPaint.setColor(colorResource.get(R.color.colorListened));
        innerPaint.setAntiAlias(true);

        listenArcPaint = new Paint();
        listenArcPaint.setColor(colorResource.get(R.color.colorAccent));
        listenArcPaint.setAntiAlias(true);

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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onPositionDoneListener = null;
        onCenterClickListener = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, size, outerPaint);

        float top = centerY - size;
        float bottom = centerY + size;
        float left = centerX - size;
        float right = centerX + size;
        arcRect.set(left, top, right, bottom);
        canvas.drawArc(arcRect, 270, (float) arcLength, true, listenArcPaint);
        canvas.drawCircle(centerX, centerY, size / 4, innerPaint);

        // Update position dial if we know the duration
        if (playerInfo.duration <= 0) {
            return;
        }
        double percent = arcLength / 360.0;
        long pos = (long) Math.ceil(percent * playerInfo.duration / 1000.0);
        long duration = (long) Math.ceil(playerInfo.duration / 1000.0);
        String elapsedTime = DateUtils.formatElapsedTime(pos);
        String totalDuration = DateUtils.formatElapsedTime(duration);
        long per = (long) Math.floor(percent * 100);
        canvas.drawText(elapsedTime + "/" + totalDuration + " (" + per + "%)", 9, 70, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouchEvent(event);
        super.onTouchEvent(event);
        return true;
    }


    /**
     * return true if between ACTION_DOWN and ACTION_UP events
     */
    public boolean getIsMoving() {
        return isMoving;
    }

    private void handleTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (isCenterAction(x, y)) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handleCenterClick();
            }
        } else if (isOuterAction(x, y)) {
            playPositionHandler.handle(x, y, event);
        }
    }

    public void setCenterColor(int colorId) {
        innerPaint.setColor(colorResource.get(colorId));
        invalidate();
    }

    /**
     * Contains information from the player's current state
     */
    public static class PlayerInfo {
        public double positionPercent;
        public double duration;
        public long currentPosition;
    }

    public void updatePlayer(PlayerInfo info) {
        playerInfo = info;
        arcLength = 360 * playerInfo.positionPercent;
        invalidate();
    }

    private class PlayPositionHandler {
        double deg = 0;
        double lastDeg = 0;
        int doubleClickState = 0;

        PlayPositionHandler() {
        }

        void handle(float x, float y, MotionEvent event) {

            float cfx = x - centerX;
            float cfy = y - centerY;
            double rads = Math.atan2(cfy, cfx) + 360;
            deg = Math.toDegrees(rads) % 360;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    lastDeg = deg;
                    break;
                case MotionEvent.ACTION_MOVE:
                    double mdd = deg - lastDeg;

                    if (Math.abs(mdd) > 300) {
                        mdd = 0;
                    }

                    arcLength += mdd;

                    if (arcLength + mdd >= 360) {
                        arcLength = 360;
                    } else if (arcLength + mdd <= 0) {
                        arcLength = 0;
                    } else {
                        arcLength += mdd;
                    }
                    lastDeg = deg;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:

                    // done moving
                    isMoving = false;

                    if (handleDoubleClick(event)) return;

                    // see if we should inform the listener
                    if (onPositionDoneListener != null) {
                        double percent = arcLength / 360;
                        onPositionDoneListener.positionChange(percent);
                    }
                    break;
            }
        }

        /**
         *
         * Move arcLength +5 deg if double click on right hand size, -5 if on left hand side.
         *
         * @param event - current event
         * @return - true if this is first of a possible double click, else false
         */
        private boolean handleDoubleClick(MotionEvent event) {
            long dt = event.getEventTime() - event.getDownTime();
            if (dt < 100) {
                doubleClickState += 1;
            } else {
                doubleClickState = 0;
            }
            if (doubleClickState == 2) {
                doubleClickState = 0;
                double ticTime = playerInfo.duration / 360;
                double degForSeconds = 15 / (ticTime / 1000);
                double tmpLength = arcLength;
                tmpLength += lastDeg < 180 ? -degForSeconds : degForSeconds;
                if (tmpLength < 0) {
                    arcLength = 0;
                } else if (tmpLength > 360) {
                    arcLength = 360;
                } else {
                    arcLength = tmpLength;
                }
            } else if (doubleClickState == 1) { // first click of a double click
                                                // or accidental tap (nothing to do)
                return true;
            }
            return false;
        }
    }

    public boolean isCenterAction (float x, float y) {
        float cfx = x - centerX;
        float cfy = y - centerY;
        double z = Math.sqrt(cfx*cfx + cfy*cfy);
        return z <= size / 4;
    }

    /**
     * Check if this action is within the outer circle (including inner circle)
     */
    public boolean isOuterAction(float x, float y) {
        float cfx = x - centerX;
        float cfy = y - centerY;
        double z = Math.sqrt(cfx*cfx + cfy*cfy);
        return z <= size;

    }

    public boolean handleCenterClick() {
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

    OnPositionDoneListener onPositionDoneListener;
    public interface OnPositionDoneListener {
        /**
         * Triggered on MotionEvent.ACTION_UP of position rotation
         *
         * @param positionPercent - the percent of arc filled
         */
        void positionChange(double positionPercent);
    }
    public void setOnPositionDoneListener(OnPositionDoneListener listener) {
        onPositionDoneListener = listener;
    }

    public OnPositionDoneListener getOnPositionDoneListener() {
        return onPositionDoneListener;
    }


}
