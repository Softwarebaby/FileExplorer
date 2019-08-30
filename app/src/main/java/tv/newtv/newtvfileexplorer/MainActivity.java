package tv.newtv.newtvfileexplorer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import tv.newtv.newtvfileexplorer.base.BaseActivity;
import tv.newtv.newtvfileexplorer.bean.FileInfo;
import tv.newtv.newtvfileexplorer.presenter.MainPresenter;
import tv.newtv.newtvfileexplorer.utils.AnimationUtil;
import tv.newtv.newtvfileexplorer.utils.DisplayUtil;
import tv.newtv.newtvfileexplorer.utils.FileUtil;
import tv.newtv.newtvfileexplorer.utils.SizeUtil;
import tv.newtv.newtvfileexplorer.view.FileAdapter;
import tv.newtv.newtvfileexplorer.view.MultipleFileInterface;
import tv.newtv.newtvfileexplorer.view.WindowManager;

public class MainActivity extends BaseActivity<MainPresenter> implements MultipleFileInterface, SearchView.OnQueryTextListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnFocusChangeListener {
    private static final String TAG = "MainActivity";
    public static final int T_DIR = 0;  //文件夹
    public static final int T_FILE = 1;  //文件
    private static final int SORT_NAME = 0;  //按名称排序
    private static final int SORT_DATE = 1;  //按日期排序
    private static final int SORT_SIZE = 2;  //按大小排序
    private static final int TYPE_FOLDER = 11;  //文件夹
    private static final int TYPE_TXT = 22;  //文本文档
    private static final int TYPE_WORD = 33;  //word文档
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 99;
    private static final String ROOT = FileUtil.SD_CARD_PATH;

    private ProgressDialog mProgressDialog;
    private FileAdapter mFileAdapter;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private PopupWindow mSpanner1, mSpanner2;
    private List<FileInfo> mList;
    private List<FileInfo> mSearchList;
    private Map<String, String> mCopyMap = new HashMap<>();  //复制文件结构
    private Comparator<FileInfo> mComparator;  //排序比较器

    private String mCurPath;  //当前路径
    private String mParentPath;  //上级目录
    private String mShowPath; // 显示目录
    private boolean mIsCut = false;
    private int asc = 1;  //排序（正序、倒序）
    private int currSort = SORT_NAME;
    private String[] sorts = {"名称", "日期", "大小"};

    //名称比较器
    private Comparator<FileInfo> nameComparator = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            Collator c = Collator.getInstance(Locale.CHINA);
            if (asc == 1)
                return c.compare(o1.getName(), o2.getName());
            else
                return c.compare(o2.getName(), o2.getName());
        }
    };
    //日期比较器
    private Comparator<FileInfo> dateComparator = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            if (o1.getLastModify() > o2.getLastModify())
                return asc;
            else if (o1.getLastModify() < o2.getLastModify())
                return -asc;
            else
                return 0;
        }
    };
    //大小比较器
    private Comparator<FileInfo> sizeComparator = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            if (o1.getByteSize() > o2.getByteSize())
                return asc;
            else if (o1.getByteSize() < o2.getByteSize())
                return -asc;
            else
                return 0;
        }
    };

    @BindView(R.id.tv_path)
    TextView pathTv;
    @BindView(R.id.list)
    ListView listView;
    @BindView(R.id.footer_bar)
    RelativeLayout footerBar;
    @BindView(R.id.pasteOnClick)
    LinearLayout pasteLayout;
    @BindView(R.id.iv_paste)
    ImageView pasteIv;
    @BindView(R.id.iv_sort)
    ImageView sortIv;
    @BindView(R.id.iv_resort)
    ImageView resortIv;
    @BindView(R.id.iv_create)
    ImageView createIv;
    @BindView(R.id.tv_sort)
    TextView sortTv;
    @BindView(R.id.tv_count)
    TextView countTv;
    @BindView(R.id.tv_size)
    TextView sizeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission();
        Log.i(TAG, "path: " + getStoragePath(this, false));
        updateData(getStoragePath(this, false));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        sortIv.setOnFocusChangeListener(this);
        resortIv.setOnFocusChangeListener(this);
        createIv.setOnFocusChangeListener(this);
        listView.setItemsCanFocus(true);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        mSpanner1 = WindowManager.Builder()
                .setParentView(resortIv)
                .setWidth(DisplayUtil.dp2px(this, 150))
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create();
        mSpanner2 = WindowManager.Builder()
                .setParentView(createIv)
                .setWidth(DisplayUtil.dp2px(this, 100))
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setParentView(createIv)
                .create();

        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void showLoading() {
        showDialog();
    }

    @Override
    public void hideLoading() {
        dismissDialog();
    }

    @Override
    public void onError(Throwable throwable) {
        Toast.makeText(this, "文件列表获取失败！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(List<FileInfo> list) {
        mList = FileUtil.getInstance().getFileGroup(list);  //结果进行分组
        mSearchList = mList;
        if (mFileAdapter == null) {
            mFileAdapter = new FileAdapter(this, mList);
            mFileAdapter.setProxy(this);
            mFileAdapter.setRelativePath(mShowPath);
            listView.setAdapter(mFileAdapter);
        } else {
            mFileAdapter.setList(mList);
            mFileAdapter.setRelativePath(mShowPath);
            mFileAdapter.notifyDataSetChanged();
        }
        updateInfoBar();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mFileAdapter.isShowChecked()) {
            onMultipleItemClick(position);
        } else {
            FileInfo item = (FileInfo) parent.getItemAtPosition(position);
            if (item.getType() == MainActivity.T_DIR) {
                updateData(item.getPath());
                mFileAdapter.notifyDataSetChanged();
            } else {
                FileUtil.getInstance().openFile(this, new File(item.getPath()));
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mFileAdapter.setCheckedVisible(true);
        mFileAdapter.notifyDataSetChanged();
        onMultipleItemClick(position);
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            AnimationUtil.doAnimation(v, 1f, 1.5f);
        } else {
            AnimationUtil.doAnimation(v, 1.5f, 1f);
        }
    }

    @Override
    public void onMultipleItemClick(int position) {
        //点击多选实现
        String value = mShowPath + "_" + position;
        if (mFileAdapter.selectSet.contains(value)) {
            mFileAdapter.selectSet.remove(value);
            if (mFileAdapter.selectSet.size() == 0) {
                footerBar.setVisibility(View.GONE);
                mFileAdapter.setCheckedVisible(false);
            } else {
                footerBar.setVisibility(View.VISIBLE);
            }
        } else {
            footerBar.setVisibility(View.VISIBLE);
            mFileAdapter.selectSet.add(value);
        }
        mFileAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (mFileAdapter.isShowChecked()) {
                    mFileAdapter.setCheckedVisible(false);
                } else {
                    mFileAdapter.setCheckedVisible(true);
                }
                mFileAdapter.notifyDataSetChanged();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        FileUtil.KEY = query.trim();
        mSearchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        FileUtil.KEY = newText.trim();
        mList = getSearchResult(newText.trim());
        resort();
        mList = mSearchList;
        return true;
    }

    @OnClick(R.id.sort_box)
    public void sortFiles() {
        resort();
        asc *= (-1);
    }

    @OnClick(R.id.iv_resort)
    public void resortFiles() {

    }

    @OnClick(R.id.iv_create)
    public void createFile() {

    }

    @OnClick(R.id.copyOnClick)
    public void copy() {
        if (mFileAdapter.selectSet.size() > 0) {
            mCopyMap.clear();
            for (Iterator<String> it = mFileAdapter.selectSet.iterator(); it.hasNext(); ) {
                String key = it.next();
                int position = getPosition(key);
                mCopyMap.put(key, mList.get(position).getPath());
            }
            pasteIv.setImageResource(R.drawable.ic_paste_holo_light);
            Toast.makeText(this, mCopyMap.size() + "个项目已复制", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "您还没选中任何项目！", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.cutOnClick)
    public void cut() {
        if (mFileAdapter.selectSet.size() > 0) {
            mCopyMap.clear();
            for (Iterator<String> it = mFileAdapter.selectSet.iterator(); it.hasNext(); ) {
                String key = it.next();
                int position = getPosition(key);
                mCopyMap.put(key, mList.get(position).getPath());
            }
            mIsCut = true;
            pasteIv.setImageResource(R.drawable.ic_paste_holo_light);
            Toast.makeText(this, mCopyMap.size() + "个项目已剪切", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "您还没选中任何项目！", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.pasteOnClick)
    public void paste() {
        if (mCopyMap.size() > 0) {
            for (Iterator<Map.Entry<String, String>> it = mCopyMap.entrySet().iterator(); it.hasNext(); ) {
                String path = it.next().getValue();
                File file = new File(path);
                if (mIsCut) {
                    deleteData();
                    mIsCut = false;
                }
                int res;
                if (file.isFile()) {
                    res = FileUtil.getInstance().pasteFile(mCurPath, new File(path));
                } else {
                    res = FileUtil.getInstance().pasteDir(mCurPath, new File(path));
                }
                if (res == 1) {
                    Toast.makeText(this, "项目已存在", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "粘贴成功", Toast.LENGTH_SHORT).show();
                    mFileAdapter.selectSet.clear();
                }
            }
            mCopyMap.clear();
            updateData(mCurPath);
            mFileAdapter.notifyDataSetChanged();
            pasteIv.setImageResource(R.drawable.ic_paste);
        } else {
            Toast.makeText(this, "您还没选中任何项目！", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.deleteOnClick)
    public void delete() {
        if (mFileAdapter.selectSet.size() == 0) {
            Toast.makeText(this, "您还没选中任何项目！", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您确定要删除这" + mFileAdapter.selectSet.size() + "个项目")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (Iterator<String> it = mFileAdapter.selectSet.iterator(); it.hasNext(); ) {
                                String key = it.next();
                                int position = getPosition(key);
                                String path = mList.get(position).getPath();
                                File file = new File(path);
                                if (file.isFile()) {
                                    FileUtil.getInstance().deleteFile(path);
                                } else {
                                    FileUtil.getInstance().deleteDir(path);
                                }
                            }
                            mFileAdapter.selectSet.clear();
                            updateData(mCurPath);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
    }

    @OnClick(R.id.selectAllOnClick)
    public void selectAll() {
        mFileAdapter.selectSet.clear();
        for (int i = 0; i < mList.size(); i++) {
            mFileAdapter.selectSet.add(mShowPath + "_" + i);
        }
        mFileAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.cancelOnClick)
    public void cancel() {
        mFileAdapter.selectSet.clear();
        footerBar.setVisibility(View.GONE);
        mFileAdapter.setCheckedVisible(false);
        mFileAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        if (mSearchView != null) {
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setSubmitButtonEnabled(true);
            mSearchView.setQueryHint("请输入文件名");
            mSearchView.setOnQueryTextListener(this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sort_name:
                currSort = SORT_NAME;
                break;
            case R.id.sort_date:
                currSort = SORT_DATE;
                break;
            case R.id.sort_size:
                currSort = SORT_SIZE;
                break;
            case R.id.c1:
                showCreateDialog(TYPE_FOLDER);
                break;
            case R.id.y1:
                showCreateDialog(TYPE_TXT);
                break;
            case R.id.y2:
                showCreateDialog(TYPE_WORD);
                break;
            default:
                break;
        }
        resort();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mCurPath.equals(ROOT)) {
            exitApp();
        } else {
            updateData(mParentPath);
            mFileAdapter.notifyDataSetChanged();
        }
    }

    private void updateData(String path) {
        mCurPath = path;
        File file = new File(path);
        mParentPath = file.getParent();
        mShowPath = submitPath(path);
        pathTv.setText(mShowPath);
        mPresenter.findFiles(path);
    }

    private void deleteData() {
        for (Iterator<Map.Entry<String, String>> it = mCopyMap.entrySet().iterator(); it.hasNext(); ) {
            String path = it.next().getValue();
            File file = new File(path);
            if (file.isFile()) {
                FileUtil.getInstance().deleteFile(path);
            } else {
                FileUtil.getInstance().deleteDir(path);
            }
        }
    }

    private void resort() {
        if (currSort == SORT_NAME) {
            mComparator = nameComparator;
        }
        if (currSort == SORT_DATE) {
            mComparator = dateComparator;
        }
        if (currSort == SORT_SIZE) {
            mComparator = sizeComparator;
        }
        Collections.sort(mList, mComparator);
        mList = FileUtil.getInstance().getFileGroup(mList);
        mFileAdapter.setList(mList);
        mFileAdapter.notifyDataSetChanged();
        updateInfoBar();
    }

    private void updateInfoBar() {
        if (asc == 1) {
            sortIv.setImageResource(R.drawable.ic_sort_up);
        } else {
            sortIv.setImageResource(R.drawable.ic_sort_down);
        }
        sortTv.setText("排序: " + sorts[currSort]);
        countTv.setText("文件数: " + mList.size());
        sizeTv.setText("大小: " + getFileSize());
    }

    private String submitPath(String path) {
        if (path.equals(ROOT)) {
            return "/";
        } else {
            return mCurPath.substring(ROOT.length());
        }
    }

    private void showCreateDialog(final int type) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog, null);
        ImageView icon1 = view.findViewById(R.id.icon1);
        if (type != TYPE_FOLDER) {
            icon1.setImageResource(type == TYPE_TXT ? R.drawable.img_txt : R.drawable.img_doc);
        }
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText text = view.findViewById(R.id.name1);
                        String name = text.getText().toString();
                        String path = mCurPath + "/" + name;
                        if (type == TYPE_TXT) {
                            path += ".txt";
                        } else if (type == TYPE_WORD) {
                            path += ".doc";
                        }
                        File destDir = new File(path);
                        if (!destDir.exists()) {
                            if (type == TYPE_FOLDER) {
                                destDir.mkdirs();
                            } else {
                                try {
                                    destDir.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        updateData(mCurPath);
                        mFileAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void showDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("正在加载文件列表，请耐心等地...");
        mProgressDialog.show();
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void exitApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.stat_sys_warning);
        builder.setMessage("确定要退出吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        positiveButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    positiveButton.setBackgroundColor(Color.parseColor("#c9c9c9"));
                } else {
                    positiveButton.setBackgroundColor(Color.parseColor("#ffffff"));
                }
            }
        });
        negativeButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    negativeButton.setBackgroundColor(Color.parseColor("#c9c9c9"));
                } else {
                    negativeButton.setBackgroundColor(Color.parseColor("#ffffff"));
                }
            }
        });
    }

    private String getFileSize() {
        long fileSizeSum = 0;
        for (FileInfo fileInfo : mList) {
            fileSizeSum += fileInfo.getByteSize();
        }
        return SizeUtil.getSize((float) fileSizeSum);
    }

    private int getPosition(String key) {
        String posStr = key.substring(key.indexOf("_") + 1);
        return Integer.valueOf(posStr);
    }

    private List<FileInfo> getSearchResult(String keyword) {
        List<FileInfo> searchResultList = new ArrayList<>();
        for (FileInfo fileInfo : mList) {
            if (fileInfo.getName().toLowerCase().contains(keyword.toLowerCase())) {
                searchResultList.add(fileInfo);
            }

        }
        return searchResultList;
    }

    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param context    上下文
     * @param isRemove   是否可移除，false返回内部存储，true返回外置sd卡
     * @return
     */
    private String getStoragePath(Context context, boolean isRemove) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object[] volumes = (Object[]) getVolumeList.invoke(mStorageManager);
            for (Object volume : volumes) {
                String path = (String) getPath.invoke(volume);
                boolean removable = (boolean) isRemovable.invoke(volume);
                if (isRemove == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
//            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
        }
    }
}
