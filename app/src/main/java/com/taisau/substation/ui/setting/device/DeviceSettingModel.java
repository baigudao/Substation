package com.taisau.substation.ui.setting.device;

import com.taisau.substation.SubstationApplication;
import com.taisau.substation.util.FileUtils;
import com.taisau.substation.util.Preference;

import java.io.File;

import static com.taisau.substation.util.Constant.LIB_DIR;

/**
 * Created by Administrator on 2017/8/21 0021
 */

public class DeviceSettingModel implements IDeviceSettingModel {
    private File file[] = new File[3];
   

    public DeviceSettingModel() {
        file[0] = new File(LIB_DIR + "/card_fea");
//        file[1] = new File(LIB_DIR + "/face_fea");
        file[1] = new File(LIB_DIR + "/face_img");
        file[2] = new File(LIB_DIR + "/card_img");
    }

    @Override
    public String getCurrentStatus(int flag) {
        String res;
        switch (flag) {
            case 0:
                res = Preference.getDevSno();
                break;
            case 1:
                res = Preference.getServerIp();
                break;
            case 2:
                res = Preference.getServerPort();
                break;
            case 3:
                res = getFileMemorySize();
                break;
            case 4:
                res = "0B";
                break;
            case 5:
                res = Preference.getDevProvince();
                if (res == null)
                    res = "请选择省";
//                Log.e("DeviceSettingModel", "获取省名称：res=" + res);
                break;
            case 6:
                res = Preference.getDevCity();
                if (res == null)
                    res = "请选择市";
//                Log.e("DeviceSettingModel", "获取市名称：res=" + res);
                break;
            case 7:
                res = Preference.getDevTownShip();
                if (res == null)
                    res = "请选择县";
//                Log.e("DeviceSettingModel", "获取县名称：res=" + res);
                break;
            default:
                res = "";
                break;
        }
        return res;
    }

    @Override
    public void setStatusChange(int pos, String content) {
        switch (pos) {
            case 0:
                Preference.setDevSno(content);
                break;
            case 1:
                Preference.setServerIp(content);
                Preference.setServerUrl("http://" + Preference.getServerIp() + ":" + Preference.getServerPort() + "/FaceNew/");
                break;
            case 2:
                Preference.setServerPort(content);
                Preference.setServerUrl("http://" + Preference.getServerIp() + ":" + Preference.getServerPort() + "/FaceNew/");
                break;
            case 5:
                if(content.contains("请选择")){
                    content = null;
                }
                Preference.setDevProvince(content);
                Preference.setDevCity(null);
                Preference.setDevTownShip(null);
                break;
            case 6:
                if(content.contains("请选择")){
                    content = null;
                }
                Preference.setDevCity(content);
                Preference.setDevTownShip(null);
                break;
            case 7:
                if(content.contains("请选择")){
                    content = null;
                }
                Preference.setDevTownShip(content);
                break;
            default:
                break;
        }
    }


    @Override
    public void clearSoft() {
        SubstationApplication.getApplication().getDaoSession().getHistoryDao().deleteAll();
        deleteFile(file);
        Preference.setHisLastId("0");
        Preference.setHisFirstId("1");
    }

    @Override
    public void clearList() {
        SubstationApplication.getApplication().getDaoSession().getPersonDao().deleteAll();
    }

    public void deleteFile(File[] fileList) {
        int totalLength  = 0;
        for (File file:fileList) {
            totalLength +=file.list().length;
        }
        String[] fileNames = new String[totalLength];
        int currentLength = 0;
        for (File file : fileList) {
            String[] temp  = file.list();
            for(int i = 0; i < temp.length; i++){
                temp[i] = file.getAbsolutePath()+"/"+temp[i];
            }
            System.arraycopy(temp,0,fileNames,currentLength,file.list().length);
            currentLength += file.list().length;
            FileUtils.deleteDirectory(file.getAbsolutePath());
            file.mkdir();
        }
        FileUtils.updateFileToSystem(SubstationApplication.getApplication(),fileNames,null);
    }

    public String getFileMemorySize() {
        long totalLength  = 0;
        for (File file1:file) {
            totalLength += FileUtils.getFolderSize(file1);
        }

        String memorySize = "";
        if (totalLength > 1024 && totalLength < 1024 * 1024)
            memorySize = totalLength / 1024 + "KB";
        else if (totalLength > 1024 * 1024 && totalLength < 1024 * 1024 * 1024)
            memorySize = totalLength / 1024 / 1024 + "MB";
        else if (totalLength > 1024 * 1024 * 1024)
            memorySize = totalLength / 1024 / 1024 / 1024 + "GB";
        else
            memorySize = totalLength + "B";
        return memorySize;
    }

    @Override
    public void checkSerialNum( String num) {

    }
}
