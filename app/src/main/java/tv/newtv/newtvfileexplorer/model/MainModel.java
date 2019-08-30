package tv.newtv.newtvfileexplorer.model;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import tv.newtv.newtvfileexplorer.bean.FileInfo;
import tv.newtv.newtvfileexplorer.contract.MainContract;
import tv.newtv.newtvfileexplorer.utils.FileUtil;


/**
 * Created by Du Senmiao on 2019/07/15
 */
public class MainModel implements MainContract.Model {

    @Override
    public Flowable<List<FileInfo>> load(final String path) {
        return Flowable.create(new FlowableOnSubscribe<List<FileInfo>>() {
            @Override
            public void subscribe(FlowableEmitter<List<FileInfo>> emitter) throws Exception {
                emitter.onNext(FileUtil.getInstance().getFileInfoList(path));
            }
        }, BackpressureStrategy.ERROR);
    }
}
