package com.taisau.substation.util;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Administrator on 2017/2/22 0022.
 */

public class FileUtils {
    public static boolean moveConfigFile(Context context, int rId, String path) {
        try {
            File fileFif = new File(path);
            if (!fileFif.exists()) {
                fileFif.createNewFile();
                OutputStream output = new FileOutputStream(fileFif);
                InputStream input = context.getResources().openRawResource(rId);
                int length = input.available();
                byte[] bts = new byte[length];
                input.read(bts);
                output.write(bts);
                input.close();
                output.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Logger.d("算法库配置文件创建失败file:" + path);
            return false;
        }
    }


    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(java.io.File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }


    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 删除多个文件
     *
     * @param filePaths 被删除文件的文件名
     */
    public static void deleteFile(List<String> filePaths) {
        for (String filePath : filePaths) {
            deleteFile(filePath);
        }
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * @param fromFile 被复制的文件
     * @param toFile   复制的目录文件
     * @param rewrite  是否重新创建文件
     */
    public static void copyFile(File fromFile, File toFile, Boolean rewrite) {

        if (!fromFile.exists()) {
            return;
        }

        if (!fromFile.isFile()) {
            return;
        }
        if (!fromFile.canRead()) {
            return;
        }
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists() && rewrite) {
            toFile.delete();
        }

        FileInputStream fosfrom = null;
        FileOutputStream fosto = null;
        try {
            fosfrom = new FileInputStream(fromFile);
            fosto = new FileOutputStream(toFile);

            byte[] bt = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭输入、输出流
            try {
                if (fosfrom != null) {
                    fosfrom.close();
                }
                if (fosto != null) {
                    fosto.flush();
                    fosto.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void wirteToFile(String path, String content) {
        File file = new File(path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //删除文件后, 通知媒体库更新文件夹
    public static void updateFileToSystem(Context context, String[] fileNames, MediaScannerConnection.OnScanCompletedListener listener) {
        MediaScannerConnection.scanFile(context, fileNames, null, listener);
    }

    public static String bytes2HexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            builder.append(buffer);
        }
        return builder.toString();
    }

    public static String hex2Decimal(String hex) {
        StringBuilder builder = new StringBuilder();
        if (hex.length() == 8) {
            for (int i = 0; i < 4; i++) {
                String str = hex.substring(hex.length() - 2 * (i + 1), hex.length() - 2 * i);
                builder.append(str);
            }
        }
        String decimal = String.valueOf(Long.parseLong(builder.toString(), 16));
        while (decimal.length() < 10) {
            decimal = "0" + decimal;
        }

        return decimal;
    }
}
