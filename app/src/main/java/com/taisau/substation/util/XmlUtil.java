package com.taisau.substation.util;

import android.util.Xml;

import com.orhanobut.logger.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by whx on 2018-03-08
 */

public class XmlUtil {

    /**
     * 获取 U盘 路径
     */
    public static String getPath() {
        String path = null;
        File storage = new File("/mnt/usb_storage");
        if (storage.exists()) {
            File[] files = storage.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {// /mnt/usb_storage/USB_DISK2
                    for (File file1 : file.listFiles()) {
                        if (file1.isDirectory()) {// /mnt/usb_storage/USB_DISK2/udisk0
                            if ((file1.listFiles()) != null) {
                                path = file1.getAbsolutePath();
                                Logger.d(" path  = " + path);
                                for (File file2 : file1.listFiles()) {
                                    Logger.d("   = " + file2.getAbsolutePath());
                                }
                                path = path + "/config.xml";
                                if (new File(path).exists()) {
                                    Logger.d("内    return path = " + path);
                                    return path;
                                }
                            }
                        }
                    }
                }
            }
        }
        Logger.d("外   return path = " + path);
        return path;
    }

    /**
     * 向SD卡写入一个XML文件
     *
     * @param path    U盘路径 不为null
     * @param name    设备名 不为null
     * @param ip      ip地址 例如：192.168.2.101
     * @param doorway 闸机进出方向，1为入口  2为出口
     */
    public static void saveXml(String path, String name, String ip, int doorway) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            // 获得一个序列化工具
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "utf-8");
            // 设置文件头
            serializer.startDocument("utf-8", true);
            serializer.startTag(null, "config");

            // 设备名
            serializer.startTag(null, "name");
            serializer.text(name);
            serializer.endTag(null, "name");
            // ip
            serializer.startTag(null, "ip");
            serializer.text(ip);
            serializer.endTag(null, "ip");
            // 进出口
            serializer.startTag(null, "doorway");
            serializer.text(String.valueOf(doorway));
            serializer.endTag(null, "doorway");

            serializer.endTag(null, "config");
            serializer.endDocument();
            fos.flush();
            fos.close();
            Logger.i("写 XML 结束");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("写 XML 文件异常");
        }
    }


    /**
     * 读取SD卡中的XML文件,使用pull解析
     *
     * @param path U盘路径，不为null
     */
    public static List<String> readXml(String path) {
        List<String> config = new ArrayList<>();
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);

            // 获得pull解析器对象
            XmlPullParser parser = Xml.newPullParser();
            // 指定解析的文件和编码格式
            parser.setInput(fis, "utf-8");

            int eventType = parser.getEventType(); // 获得事件类型
            String name = null;
            String ip = null;
            String port = null;
            String doorway = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName(); // 获得当前节点的名称

                switch (eventType) {
                    case XmlPullParser.START_TAG: // 当前等于开始节点
                        if ("config".equals(tagName)) {
                        } else if ("name".equals(tagName)) { // <name>
                            name = parser.nextText();
                            config.add(name);
                        } else if ("ip".equals(tagName)) { // <ip>
                            ip = parser.nextText();
                            config.add(ip);
                        } else if ("port".equals(tagName)) { // <port>
                            port = parser.nextText();
                            config.add(port);
                        } else if ("doorway".equals(tagName)) { // <doorway>
                            doorway = parser.nextText();
                            config.add(doorway);
                        }
                        break;
                    case XmlPullParser.END_TAG: // </persons>
                        if ("config".equals(tagName)) {
                            Logger.i("name---" + name);
                            Logger.i("ip---" + ip);
                            Logger.i("port---" + port);
                            Logger.i("doorway---" + doorway);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next(); // 获得下一个事件类型
            }
        } catch (Exception e) {
            e.printStackTrace();
            config.clear();
        }
        return config;
    }
}

