package tv.newtv.newtvfileexplorer.bean;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class FileInfo {
    private String name;
    private String size;
    private String path;
    private String time;
    private int icon;
    private int type;
    private long lastModify;
    private long byteSize;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setLastModify(long lastModify) {
        this.lastModify = lastModify;
    }

    public long getLastModify() {
        return lastModify;
    }

    public void setByteSize(long byteSize) {
        this.byteSize = byteSize;
    }

    public long getByteSize() {
        return byteSize;
    }
}
