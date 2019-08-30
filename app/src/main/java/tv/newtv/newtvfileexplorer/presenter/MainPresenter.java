package tv.newtv.newtvfileexplorer.presenter;

import android.annotation.SuppressLint;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import tv.newtv.newtvfileexplorer.base.BasePresenter;
import tv.newtv.newtvfileexplorer.bean.FileInfo;
import tv.newtv.newtvfileexplorer.contract.MainContract;
import tv.newtv.newtvfileexplorer.model.MainModel;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
    private MainContract.Model mModel;

    public MainPresenter() {
        mModel = new MainModel();
    }

    @SuppressLint("CheckResult")
    @Override
    public void findFiles(String path) {
        if (!isAttach()) return;
        mView.showLoading();
        mModel.load(path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<FileInfo>>() {
                               @Override
                               public void accept(List<FileInfo> list) throws Exception {
                                   mView.onSuccess(list);
                                   mView.hideLoading();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                    mView.onError(throwable);
                                    mView.hideLoading();
                               }
                           }
                );
    }
}
