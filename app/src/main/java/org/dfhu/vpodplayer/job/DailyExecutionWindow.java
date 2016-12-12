package org.dfhu.vpodplayer.job;

import java.util.concurrent.TimeUnit;

public class DailyExecutionWindow {
    public final long startMs;
    public final long endMs;

    /**
     * Holds the start end time in ms for a job.
     * Will wrap around to next day if hour < targetHour.
     * @param hour - current hour
     * @param minute - current minute
     * @param targetHour - hour we want to start
     * @param targetMinute - minute we want to start
     * @param windowLengthInMinutes - number of minutes for the execution window
     */
    public DailyExecutionWindow(int hour, int minute, long targetHour, long targetMinute, long windowLengthInMinutes) {
        long hourOffset;
        long minuteOffset;

        if (targetHour >= hour) {
            hourOffset = TimeUnit.HOURS.toMillis(targetHour - hour);
        } else {
            hourOffset = TimeUnit.HOURS.toMillis((24 + targetHour) - hour);
        }

        if (targetMinute >= minute) {
            minuteOffset = TimeUnit.MINUTES.toMillis(targetMinute - minute);
        } else {
            minuteOffset = TimeUnit.MINUTES.toMillis((60 + targetMinute) - minute);
        }

        this.startMs = hourOffset + minuteOffset;
        this.endMs = this.startMs + TimeUnit.MINUTES.toMillis(windowLengthInMinutes);

    }
}
