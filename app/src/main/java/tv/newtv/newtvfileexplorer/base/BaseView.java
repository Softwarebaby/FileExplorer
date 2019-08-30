package tv.newtv.newtvfileexplorer.base;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public interface BaseView {
    void showLoading();
    void hideLoading();
    void onError(Throwable throwable);
}
