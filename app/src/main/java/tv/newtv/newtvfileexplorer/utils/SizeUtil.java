package tv.newtv.newtvfileexplorer.utils;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class SizeUtil {
    public static final long KB = 1024;
    public static final long MB = 1024 * KB;
    public static final long GB = 1024 * MB;

    private SizeUtil() {}

    public static String getSize(float length) {
        String result;
        if (length < KB) {
            result = String.format("%dB", (int) length);
        } else if (length < MB) {
            result = String.format("%.2fKB", length / KB);
        } else if (length < GB) {
            result = String.format("%.2fMB", length / MB);
        } else {
            result = String.format("%.2fGB", length / GB);
        }
        return result;
    }
}
