package com.taisau.substation.ui.setting.display;

import com.taisau.substation.listener.OnServerSettingChangeListener;
import com.taisau.substation.util.Constant;
import com.taisau.substation.util.FileUtils;
import com.taisau.substation.util.Preference;

import java.io.File;


/**
 * Created by Administrator on 2017/8/18 0018
 */

public class DisplaySettingModel implements IDisplaySettingModel {
    @Override
    public String getCurrentDisplayContent(int flag) {
        String res;
        switch (flag) {
            case 0:
                res = Preference.getCustomName();
                break;
            case 1:
                res = Preference.getMainTittle();
                break;
            case 2:
                res = Preference.getSubTittle();
                break;
            case 3:
                res = Preference.getAdsPath();
                break;
             case 4:
                res = Preference.getDoorway();
                if(res==null){
                    res = "1";
                }
                break;
            default:
                res = "";
                break;
        }
        return res;
    }

    @Override
    public String uploadPicture(String filePath) {
        return null;
    }

    @Override
    public String restoreDefaultPicture() {
        String result = "success,ok";
        try {
            Preference.setAdsPath(null);
        } catch (Exception e) {
            e.printStackTrace();
            result = "error," + e.getMessage();
        }
        return result;
    }

    @Override
    public String getUsbAdPicture() {
        //待验证是否是所有U盘的目录都是  /mnt/usb_storage/USB_DISK2/udisk0  如果不是的话就需要遍历了。
//        File storage = new File("/mnt/usb_storage/USB_DISK2/udisk0");
        File storage = new File("/mnt/usb_storage");
        if (!storage.exists()) {
            return null;
        }
        String result = null;
        File[] files = storage.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {// /mnt/usb_storage/USB_DISK2
                for (File file1 : file.listFiles()) {
                    if (file1.isDirectory()) {// /mnt/usb_storage/USB_DISK2/udisk0
                        for (File file2 : file1.listFiles()) {
                            result = getUSBPicturePath(file2);
                            if (!result.equals("")) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private String getUSBPicturePath(File file) {
        if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".bmp")) {
            String name = file.getName();
            File parentPath = new File(Constant.ADS_IMG);
            if (!parentPath.exists()) {
                parentPath.getParentFile().mkdir();
            } else if (parentPath.listFiles().length > 0) {
                for (File file1 : parentPath.listFiles()) {
                    file1.delete();
                }
            }
            File copyFile = new File(Constant.ADS_IMG + "/" + name);
            FileUtils.copyFile(file, copyFile, true);
            return copyFile.getAbsolutePath();
        }
        return "";
    }

    @Override
    public void setDisplayContentChange(int pos, String content) {
        switch (pos) {
            case 0:
                Preference.setCustomName(content);
                OnServerSettingChangeListener.OnSettingChange("name");
                break;
            case 1:
                Preference.setMainTittle(content);
                break;
            case 2:
                Preference.setSubTittle(content);
                break;
            case 3:
                Preference.setAdsPath(content);
                break;
              case 4:
                Preference.setDoorway(content);
                break;

            default:
                break;
        }
    }


}
