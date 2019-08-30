package tv.newtv.newtvfileexplorer.view;

import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by Du Senmiao on 2019/07/22
 */
public class WindowManager {
    private static WindowManager INSTANCE = null;
    public PopupWindow mPopupWindow;
    private View mParentView;
    private int mWidth = 0;
    private int mHeight = 0;

    public static WindowManager Builder() {
        if (INSTANCE == null) {
            INSTANCE = new WindowManager();
        }
        return INSTANCE;
    }

    public WindowManager setParentView(View parentView) {
        mParentView = parentView;
        return this;
    }

    public WindowManager setWidth(int width) {
        mWidth = width;
        return this;
    }

    public WindowManager setHeight(int height) {
        mHeight = height;
        return this;
    }

    public PopupWindow create() {
        if (mParentView == null) {
            mPopupWindow = new PopupWindow();
        } else {
            if (mWidth == 0 && mHeight == 0) {
                mPopupWindow = new PopupWindow(mParentView);
            } else{
                mPopupWindow = new PopupWindow(mParentView, mWidth, mHeight);
            }
        }
        return mPopupWindow;
    }

}
