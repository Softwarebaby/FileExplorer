package tv.newtv.newtvfileexplorer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class TimeUtil {
    private static SimpleDateFormat simpleDateFormat;

    private TimeUtil() {}

    public static String format(long date) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
        Date formatDate = new Date(date);
        return simpleDateFormat.format(formatDate);
    }
}
