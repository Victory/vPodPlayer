package org.dfhu.vpodplayer.util;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.TimeZone;

public class DateUtil {
    private final Context context;

    private Calendar calender = Calendar.getInstance();
    private TimeZone timeZone = calender.getTimeZone();
    private int timeZoneOffset = timeZone.getRawOffset();

    public DateUtil(Context context) {
       this.context = context;
    }
    public String localFromUnixTime(long unixTime) {
        if (unixTime == 0) {
            return "";
        }

        long tic = unixTime + timeZoneOffset;
        return DateFormat.getDateFormat(context).format(tic);
    }
}
