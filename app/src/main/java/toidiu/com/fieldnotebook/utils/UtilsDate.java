package toidiu.com.fieldnotebook.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.TimeUnit;

/**
 * Created by toidiu on 4/13/16.
 */
public class UtilsDate {

    public static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMMM dd, yyyy");

    public static boolean isDayOld(DateTime dateTime){
        DateTime now = DateTime.now();
        DateTime dayBeforeNow = now.minus(TimeUnit.DAYS.toMillis(1));
        return dayBeforeNow.isAfter(dateTime);
    }
}
