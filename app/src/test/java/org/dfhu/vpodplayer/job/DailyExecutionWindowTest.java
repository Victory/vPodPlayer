package org.dfhu.vpodplayer.job;

import org.junit.Assert;
import org.junit.Test;

public class DailyExecutionWindowTest extends Assert {
    private int nowHour = 15;
    private int nowMinute = 30;

    @Test
    public void testLaterToday() {
        int targetHour = 16;
        int targetMinute = 45;
        int windowLengthInMinutes = 5;
        long expectedStartMs = 1000L * (3600L + 15L * 60L);
        long expectedEndMs = expectedStartMs + windowLengthInMinutes * 60000L;

        DailyExecutionWindow e = new DailyExecutionWindow(
               nowHour, nowMinute, targetHour, targetMinute, windowLengthInMinutes);
        assertEquals("startMs", expectedStartMs, e.startMs);
        assertEquals("endMs", expectedEndMs, e.endMs);
    }

    @Test
    public void earlierInTheDay() {
        int targetHour = 14;
        int targetMinute = 15;
        int windowLengthInMinutes = 5;
        long expectedStartMs = 1000L * (3600L * 23L + 45L * 60L);
        long expectedEndMs = expectedStartMs + windowLengthInMinutes * 60000;

        DailyExecutionWindow e = new DailyExecutionWindow(
               nowHour, nowMinute, targetHour, targetMinute, windowLengthInMinutes);
        assertEquals("startMs", expectedStartMs, e.startMs);
        assertEquals("endMs", expectedEndMs, e.endMs);
    }

    @Test
    public void laterInCurrentHour() {
        int targetHour = 15;
        int targetMinute = 45;
        int windowLengthInMinutes = 5;
        long expectedStartMs = 1000L * (15L * 60L);
        long expectedEndMs = expectedStartMs + windowLengthInMinutes * 60000;

        DailyExecutionWindow e = new DailyExecutionWindow(
               nowHour, nowMinute, targetHour, targetMinute, windowLengthInMinutes);
        assertEquals("startMs", expectedStartMs, e.startMs);
        assertEquals("endMs", expectedEndMs, e.endMs);
    }

    @Test
    public void rightNow() {
        int targetHour = 15;
        int targetMinute = 30;
        int windowLengthInMinutes = 5;
        long expectedStartMs = 0;
        long expectedEndMs = expectedStartMs + windowLengthInMinutes * 60000;

        DailyExecutionWindow e = new DailyExecutionWindow(
               nowHour, nowMinute, targetHour, targetMinute, windowLengthInMinutes);
        assertEquals("startMs", expectedStartMs, e.startMs);
        assertEquals("endMs", expectedEndMs, e.endMs);
    }
}
