package tv.newtv.newtvfileexplorer.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import tv.newtv.newtvfileexplorer.contract.MainContract;

/**
 * Created by Du Senmiao on 2019/07/16
 */
public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity implements MainContract.View {
    protected T mPresenter;
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.getLayoutId());
        unbinder = ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        unbinder.unbind();
        super.onDestroy();
    }

    /**
     * 设置布局
     * @return
     */
    public abstract int getLayoutId();

    /**
     * 初始化视图
     * @return
     */
    public abstract void initView();
}
