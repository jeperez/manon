package manon.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.apache.commons.lang3.StringUtils.repeat;

/**
 * Utility methods related to now and time.
 */
public final class Tools {
    
    public static final String MDC_KEY_ENV = "env";
    public static final String MDC_KEY_REQUEST_ID = "r";
    public static final String MDC_KEY_USER = "u";
    
    /** {@value} media type. */
    public static final String MEDIA_JSON = "application/json";
    
    /** {@value} date format. */
    public static final String DATE_FORMAT = "yyyy-MM-ddHH:mm:ssS Z";
    
    public static final ObjectMapper JSON;
    
    static {
        JSON = new ObjectMapper();
        JSON.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
    }
    
    /** Get current date. */
    public static Date now() {
        return new Date();
    }
    
    /**
     * Get date with given year, month, day, hours, minutes, seconds and milliseconds.
     * @param year year.
     * @param month month (0-11).
     * @param dayOfMonth day of month (1-*).
     * @param hourOfDay hours (0-23).
     * @param minute minutes (0-59).
     * @param second seconds (0-59).
     * @param millisecond milliseconds (0-999).
     */
    public static Date date(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millisecond) {
        return calendar(year, month, dayOfMonth, hourOfDay, minute, second, millisecond).getTime();
    }
    
    /**
     * Get date with given year, month, day, hours, minutes, seconds and milliseconds.
     * @param year year.
     * @param month month (0-11).
     * @param dayOfMonth day of month (1-*).
     * @param hourOfDay hours (0-23).
     * @param minute minutes (0-59).
     * @param second seconds (0-59).
     * @param millisecond milliseconds (0-999).
     */
    public static Calendar calendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millisecond) {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return cal;
    }
    
    /**
     * Get today's date with given hours, minutes, seconds and milliseconds.
     * @param hourOfDay hours (0-23).
     * @param minute minutes (0-59).
     * @param second seconds (0-59).
     * @param millisecond milliseconds (0-999).
     */
    public static Calendar today(int hourOfDay, int minute, int second, int millisecond) {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return cal;
    }
    
    /** Get current date at 0:00'0 000. */
    public static Date startOfDay() {
        return today(0, 0, 0, 0).getTime();
    }
    
    /** Get current date at 23:59'59 999. */
    public static Date endOfDay() {
        return today(23, 59, 59, 999).getTime();
    }
    
    public static Date yesterday() {
        Calendar yesterday = new GregorianCalendar();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday.getTime();
    }
    
    /** Return {@code true} if the provided string is null or empty once trimmed, otherwise {@code false}. */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static String shortenLog(Object obj) {
        if (obj == null) {
            return "null";
        }
        String str = obj.toString();
        if (str.length() > 100) {
            return str.substring(0, 30) + "... (long string, length=" + str.length() + ")";
        }
        return str;
    }
    
    public static String shortenAndAnonymizeLog(Object obj) {
        if (obj == null) {
            return "null";
        }
        String str = obj.toString();
        if (str.length() > 100) {
            return repeat("*", 30) + "... (long string, length=" + str.length() + ")";
        }
        return repeat("*", str.length());
    }
    
    /** Get a new MongoDB ObjectId as String. */
    public static String objId() {
        return new ObjectId().toString();
    }
    
    private Tools() {
        // utility class
    }
}
