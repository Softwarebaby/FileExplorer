package tv.newtv.newtvfileexplorer.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by Du Senmiao on 2019/07/22
 */
public class DisplayUtil {

    private DisplayUtil() {}

    public static int dp2px(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
