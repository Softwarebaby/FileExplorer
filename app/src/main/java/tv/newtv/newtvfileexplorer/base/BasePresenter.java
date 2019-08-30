package tv.newtv.newtvfileexplorer.base;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class BasePresenter<V extends BaseView> {
    protected V mView;

    public void attachView(V view) {
        mView = view;
    }

    public void detachView() {
        mView = null;
    }

    public boolean isAttach() {
        return mView != null;
    }
}
