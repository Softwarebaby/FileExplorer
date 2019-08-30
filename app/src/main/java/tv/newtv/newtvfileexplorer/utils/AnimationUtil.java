package tv.newtv.newtvfileexplorer.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Du Senmiao on 2019/07/22
 */
public class AnimationUtil {
    private static AnimatorSet animatorSet = null;
    private static Interpolator interpolator = null;
    private static final float TENSION = 2.2f;
    private static final long DURATION = 400;

    private AnimationUtil () {}

    public static void doAnimation(View view, float start, float end) {
        if (animatorSet == null) {
            animatorSet = new AnimatorSet();
        }
        if (interpolator == null) {
            interpolator = new OvershootInterpolator(TENSION);
        }
        ObjectAnimator x = ObjectAnimator.ofFloat(view, "scaleX", start, end);
        ObjectAnimator y = ObjectAnimator.ofFloat(view, "scaleY", start, end);
        animatorSet.setInterpolator(interpolator);
        animatorSet.play(x).with(y);
        animatorSet.setDuration(DURATION);
        animatorSet.start();
    }
}
