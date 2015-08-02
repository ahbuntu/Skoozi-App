package com.megaphone.skoozi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ahmadulhassan on 2015-07-01.
 */
public class PresentationUtil {

    private class TimestampSeconds {
        static final long SECONDS_IN_YEAR = 31556952;
        static final long SECONDS_IN_MONTH = 2592000;
        static final long SECONDS_IN_DAY = 86400;
        static final long SECONDS_IN_HOUR = 3600;
        static final long SECONDS_IN_MIN = 60;
    }

    /**
     * Formats the unix timestamp
     *
     * @param timestamp
     * @return timestamp formatted as MMM dd, hh:mm a
     */
    public static String unixTimestampAsDateTime(long timestamp) {
        Date date = new Date(timestamp * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Calculates the age of the unix timestamp from the current time
     *
     * @param timestamp
     * @return nicely formatted string representing age
     */
    public static String unixTimestampAge(long timestamp) {
        long delta = (System.currentTimeMillis()/1000L) - timestamp;
        if (delta/TimestampSeconds.SECONDS_IN_YEAR != 0) {
            return delta/TimestampSeconds.SECONDS_IN_YEAR + " y";
        } else if (delta/TimestampSeconds.SECONDS_IN_MONTH != 0) {
            return delta/TimestampSeconds.SECONDS_IN_MONTH + " mo";
        } else if (delta/TimestampSeconds.SECONDS_IN_DAY != 0) {
            return delta/TimestampSeconds.SECONDS_IN_DAY + " d";
        } else if (delta/TimestampSeconds.SECONDS_IN_HOUR != 0) {
            return delta/TimestampSeconds.SECONDS_IN_HOUR + " h";
        } else if (delta/TimestampSeconds.SECONDS_IN_MIN != 0) {
            return delta/TimestampSeconds.SECONDS_IN_MIN + " m";
        } else {
            return delta + " sec";
        }
    }


}
