package tv.newtv.newtvfileexplorer.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tv.newtv.newtvfileexplorer.MainActivity;
import tv.newtv.newtvfileexplorer.R;
import tv.newtv.newtvfileexplorer.bean.FileInfo;
import tv.newtv.newtvfileexplorer.utils.FileUtil;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class FileAdapter extends BaseAdapter {
    private Context mContext;
    private MultipleFileInterface mProxy;
    private List<FileInfo> mList;
    public Set<String> selectSet = new HashSet<>();
    private String mRelativePath;
    private boolean isShowChecked = false;

    public FileAdapter(Context context, List<FileInfo> list) {
        mContext = context;
        mList = list;
    }

    public void setList(List<FileInfo> list) {
        mList = list;
    }

    public void setProxy(MultipleFileInterface proxy) {
        mProxy = proxy;
    }

    public void setRelativePath(String path) {
        mRelativePath = path;
    }

    public void setCheckedVisible(boolean isShowChecked) {
        this.isShowChecked = isShowChecked;
    }

    public boolean isShowChecked() {
        return isShowChecked;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_file, null);
            holder.item = convertView.findViewById(R.id.item);
            holder.icon = convertView.findViewById(R.id.icon);
            holder.name = convertView.findViewById(R.id.name);
            holder.desc = convertView.findViewById(R.id.desc);
            holder.lineOnClick = convertView.findViewById(R.id.lineOnClick);
            holder.imgOnClick = convertView.findViewById(R.id.imgOnClick);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FileInfo item = mList.get(position);
        String name = item.getName();
        String key = FileUtil.KEY;
        int start = name.toLowerCase().indexOf(key.toLowerCase());  //高亮文字的起始位置
        if (start > -1) {
            int end = start + key.length();  //高亮文字的终止位置
            // 字符串样式对象
            SpannableStringBuilder style = new SpannableStringBuilder(name);
            style.setSpan(  // 设定样式
                    new ForegroundColorSpan(Color.BLUE), // 前景样式
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE// 旗标
            );
            holder.name.setText(style);
        } else {
            holder.name.setText(name);
        }
        holder.icon.setImageResource(item.getIcon());
        holder.desc.setText(item.getSize() + " " + item.getTime());
        String value = mRelativePath + "_" + position;
        if (selectSet.contains(value)) {
            holder.imgOnClick.setImageResource(R.drawable.ic_blue_selected);
        } else {
            holder.imgOnClick.setImageResource(R.drawable.ic_blue_unselected);
        }
        holder.lineOnClick.setVisibility(isShowChecked ? View.VISIBLE : View.GONE);
        //点击多选框事件
        holder.lineOnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProxy.onMultipleItemClick(position);
            }
        });
        holder.item.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    holder.item.setBackgroundColor(Color.parseColor("#8c8c8c"));
                    holder.desc.setTextColor(Color.parseColor("#636363"));
                } else {
                    holder.item.setBackgroundColor(Color.parseColor("#2b2b2b"));
                    holder.desc.setTextColor(Color.parseColor("#9f9f9f"));
                }
            }
        });
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInfo fileInfo = mList.get(position);
                if (item.getType() == MainActivity.T_DIR) {
//                    updateData(item.getPath());
                    notifyDataSetChanged();
                } else {
                    FileUtil.getInstance().openFile(mContext, new File(fileInfo.getPath()));
                }
            }
        });
        return convertView;
    }


    class ViewHolder {
        RelativeLayout item;
        ImageView icon;
        TextView name;
        TextView desc;
        RelativeLayout lineOnClick;
        ImageView imgOnClick;
    }
}
