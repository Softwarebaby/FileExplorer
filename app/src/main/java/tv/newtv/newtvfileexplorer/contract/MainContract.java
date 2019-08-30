package tv.newtv.newtvfileexplorer.contract;

import java.util.List;

import io.reactivex.Flowable;
import tv.newtv.newtvfileexplorer.base.BaseModel;
import tv.newtv.newtvfileexplorer.base.BaseView;
import tv.newtv.newtvfileexplorer.bean.FileInfo;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public interface MainContract {
    interface Model extends BaseModel {
        Flowable<List<FileInfo>> load(String path);
    }
    interface View extends BaseView {
        @Override
        void showLoading();

        @Override
        void hideLoading();

        @Override
        void onError(Throwable throwable);

        void onSuccess(List<FileInfo> list);
    }
    interface Presenter {
        void findFiles(String path);
    }
}
