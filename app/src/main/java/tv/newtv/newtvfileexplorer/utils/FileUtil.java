package tv.newtv.newtvfileexplorer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import tv.newtv.newtvfileexplorer.MainActivity;
import tv.newtv.newtvfileexplorer.R;
import tv.newtv.newtvfileexplorer.bean.FileInfo;

/**
 * Created by Du Senmiao on 2019/07/15
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    public static final String SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String KEY = "";
    private static FileUtil INSTANCE = null;

    private FileUtil() {}

    public static FileUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileUtil();
        }
        return INSTANCE;
    }

    /**
     * 根据路径获取文件和文件夹列表
     * @param path
     * @return
     */
    public List<FileInfo> getFileInfoList(String path) {
        List<FileInfo> list = new ArrayList<>();
        File pfile = new File(path);
        File[] files;
        if (pfile.exists()) {
            files = pfile.listFiles();
        } else {
            pfile.mkdirs();
            Log.i(TAG, "file_absolute_path: " + pfile.getAbsolutePath());
            files = pfile.listFiles();
        }

        if (files != null && files.length > 0) {
            for (File file : files) {
                FileInfo item = new FileInfo();
                if (file.isHidden()) {
                    continue;
                }
                if (file.isDirectory() && file.canRead()) {  //文件夹
                    item.setIcon(R.drawable.img_folder);
                    item.setByteSize(file.length());
                    item.setSize(SizeUtil.getSize((float)item.getByteSize()));
                    item.setType(MainActivity.T_DIR);
                } else if (file.isFile()) {  // 文件
                    Log.i(TAG, "file_name: " + file.getName());
                    String ext = getFileEXT(file.getName());
                    Log.i(TAG, "file_ext: " + ext);
                    item.setIcon(getDrawableIcon(ext));
                    String size = SizeUtil.getSize((float)file.length());
                    item.setSize(size);
                    item.setType(MainActivity.T_FILE);
                } else {
                    item.setIcon(R.drawable.img_mul_file);
                }
                item.setName(file.getName());
                item.setLastModify(file.lastModified());
                item.setPath(file.getPath());
                String time = TimeUtil.format(item.getLastModify());
                item.setTime(time);
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 打开文件
     * @param context
     * @param aFile
     */
    public void openFile(Context context, File aFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String fileName = aFile.getName();
        String end = getFileEXT(fileName).toLowerCase();
        if (aFile.exists()) {  // 根据不同的文件类型来打开文件
            if (checkEndsInArray(end, new String[]{"png", "gif", "jpg", "bmp"})) {
                // 图片
                intent.setDataAndType(Uri.fromFile(aFile), "image/*");  //MIME TYPE
            } else if (checkEndsInArray(end, new String[]{"apk"})) {
                // apk
                intent.setDataAndType(Uri.fromFile(aFile), "application/vnd.android.package-archive");
            } else if (checkEndsInArray(end, new String[]{"mp3", "amr", "ogg", "mid", "wav"})) {
                // audio
                intent.setDataAndType(Uri.fromFile(aFile), "audio/*");
            } else if (checkEndsInArray(end, new String[]{"mp4", "3gp", "mpeg", "mov", "flv"})) {
                // video
                intent.setDataAndType(Uri.fromFile(aFile), "video/*");
            } else if (checkEndsInArray(end, new String[]{"txt", "ini", "log", "java", "xml", "html"})) {
                // text
                intent.setDataAndType(Uri.fromFile(aFile), "text/*");
            } else if (checkEndsInArray(end, new String[]{"doc", "docx"})) {
                // word
                intent.setDataAndType(Uri.fromFile(aFile), "application/msword");
            } else if (checkEndsInArray(end, new String[]{"xls", "xlsx"})) {
                // excel
                intent.setDataAndType(Uri.fromFile(aFile), "application/vnd.ms-excel");
            } else if (checkEndsInArray(end, new String[]{"ppt", "pptx"})) {
                // ppt
                intent.setDataAndType(Uri.fromFile(aFile), "application/vnd.ms-powerpoint");
            } else if (checkEndsInArray(end, new String[]{"chm"})) {
                // chm
                intent.setDataAndType(Uri.fromFile(aFile), "application/x-chm");
            } else {
                // 其他
                intent.setDataAndType(Uri.fromFile(aFile), "application/$end");
            }
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "没有找到适合打开此文件的应用", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * 文件分组
     * @param list
     * @return
     */
    public List<FileInfo> getFileGroup(List<FileInfo> list) {
        List<FileInfo> dirs = new ArrayList<>();
        List<FileInfo> files = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            FileInfo item = list.get(i);
            if (item.getType() == 0) {
                dirs.add(item);
            } else {
                files.add(item);
            }
        }
        dirs.addAll(files);
        return dirs;
    }

    /**
     * 删除文件
     * @param path
     */
    public void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 删除文件夹
     * @param path
     */
    public void deleteDir(String path) {
        File dir = new File(path);
        File[] files = null;
        if (dir.exists()) {
            files = dir.listFiles();
        }
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    deleteFile(file.getAbsolutePath());
                }
                if (file.isDirectory()) {
                    deleteDir(file.getAbsolutePath());
                }
            }
        }
        dir.delete();
    }

    /**
     * 粘贴文件
     * @param targetDir
     * @param file
     * @return
     */
    public int pasteFile(String targetDir, File file) {
        File newFile = new File(targetDir, file.getName());
        if (newFile.exists()) {
            return 1;
        } else {
            try {
                if (file.isFile()) {
                    newFile.createNewFile();
                } else if (file.isDirectory()) {
                    newFile.mkdirs();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cloneTarget(file, newFile);
        return 0;
    }

    /**
     * 粘贴文件夹
     * @param targetDir
     * @param dir
     * @return
     */
    public int pasteDir(String targetDir, File dir) {
        File newDir = new File(targetDir, dir.getName());
        newDir.mkdirs();
        File[] files = null;
        if (dir.exists()) {
            files = dir.listFiles();
        }
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    pasteFile(newDir.getAbsolutePath(), file);
                }
                if (file.isDirectory()) {
                    pasteDir(newDir.getAbsolutePath(), file);
                }
            }
        }
        return 0;
    }

    private void cloneTarget(File from, File to) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
            out.transferFrom(in, 0, in.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null && fos != null && in != null && out != null) {
                try {
                    fis.close();
                    fos.close();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取与扩展名对应的图片id
     * @param end
     * @return
     */
    private int getDrawableIcon(String end) {
        int id = 0;
        if (end.equals("asf")) {
            id = R.drawable.img_asf;
        } else if (end.equals("avi")) {
            id = R.drawable.img_avi;
        } else if (end.equals("bmp")) {
            id = R.drawable.img_bmp;
        } else if (end.equals("doc")) {
            id = R.drawable.img_doc;
        } else if (end.equals("gif")) {
            id = R.drawable.img_gif;
        } else if (end.equals("html")) {
            id = R.drawable.img_html;
        } else if (end.equals("apk")) {
            id = R.drawable.img_iapk;
        } else if (end.equals("ico")) {
            id = R.drawable.img_ico;
        } else if (end.equals("jpg")) {
            id = R.drawable.img_jpg;
        } else if (end.equals("log")) {
            id = R.drawable.img_log;
        } else if (end.equals("mov")) {
            id = R.drawable.img_mov;
        } else if (end.equals("mp3")) {
            id = R.drawable.img_mp3;
        } else if (end.equals("mp4")) {
            id = R.drawable.img_mp4;
        } else if (end.equals("mpeg")) {
            id = R.drawable.img_mpeg;
        } else if (end.equals("pdf")) {
            id = R.drawable.img_pdf;
        } else if (end.equals("png")) {
            id = R.drawable.img_png;
        } else if (end.equals("ppt")) {
            id = R.drawable.img_ppt;
        } else if (end.equals("rar")) {
            id = R.drawable.img_rar;
        } else if (end.equals("txt") || end.equals("dat") || end.equals("ini")
                || end.equals("java")) {
            id = R.drawable.img_txt;
        } else if (end.equals("vob")) {
            id = R.drawable.img_vob;
        } else if (end.equals("wav")) {
            id = R.drawable.img_wav;
        } else if (end.equals("wma")) {
            id = R.drawable.img_wma;
        } else if (end.equals("wmv")) {
            id = R.drawable.img_wmv;
        } else if (end.equals("xls")) {
            id = R.drawable.img_xls;
        } else if (end.equals("xml")) {
            id = R.drawable.img_xml;
        } else if (end.equals("zip")) {
            id = R.drawable.img_zip;
        } else if (end.equals("3gp") || end.equals("flv")) {
            id = R.drawable.img_file_video;
        } else if (end.equals("amr")) {
            id = R.drawable.img_file_audio;
        } else {
            id = R.drawable.img_default_fileicon;
        }
        return id;
    }

    /**
     * 检测扩展名是否在数组中
     * @param end
     * @param ends
     * @return
     */
    private boolean checkEndsInArray(String end, String[] ends) {
        for (String aEnd : ends) {
            if (end.equals(aEnd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 截取文件的扩展名
     * @param name
     * @return
     */
    private String getFileEXT(String name) {
        String fileExt = null;
        if (name.contains(".")) {
            int dot = name.lastIndexOf(".");
            fileExt = name.substring(dot + 1);
        }
        return fileExt;
    }
}
