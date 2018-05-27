package com.taisau.substation.ui.setting.display;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-08-17.
 */

public class DisplaySettingPresenter {
    private IDisplaySettingView iDisplaySettingView;
    private IDisplaySettingModel iDisplaySettingModel;

    public DisplaySettingPresenter(IDisplaySettingView view) {
        this.iDisplaySettingView = view;
        iDisplaySettingModel=new DisplaySettingModel();
    }

    public ArrayList<String> getDisplayContent() {
        ArrayList<String> contents = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                contents.add(i, iDisplaySettingModel.getCurrentDisplayContent(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            contents.clear();
            contents.add("用户名");
            contents.add("主标题");
            contents.add("副标题");
            contents.add("");
            contents.add("1");
        }
        return contents;
    }
    public void setDisplayContentChange(int position,String content){
        try {
            iDisplaySettingModel.setDisplayContentChange(position,content);
            iDisplaySettingView.showChangeResult(position,true);
        } catch (Exception e) {
            e.printStackTrace();
            iDisplaySettingView.showChangeResult(position,false);
        }
    }
    public void uploadPicture(String filePath) {
        try {
            iDisplaySettingView.showDialog();
            String result = iDisplaySettingModel.uploadPicture(filePath);
            if (result.contains("success")) {
                iDisplaySettingView.showUploadResult(true, result.split(",")[1]);
            } else {
                iDisplaySettingView.showUploadResult(false, result.split(",")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            iDisplaySettingView.showUploadResult(false, "网络异常");

        } finally {
            iDisplaySettingView.hideDialog();
        }
    }

    public void restoreDefaultPicture() {
        try {
            String result = iDisplaySettingModel.restoreDefaultPicture();
            if (result.contains("success")) {
                iDisplaySettingView.showRestoreDefaultPictureResult(true, result.split(",")[1]);
            } else {
                iDisplaySettingView.showRestoreDefaultPictureResult(false, result.split(",")[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            iDisplaySettingView.showRestoreDefaultPictureResult(false, "网络异常");
        }
    }
    void getUsbAdPicture(){
        String adPath = iDisplaySettingModel.getUsbAdPicture();
        if(adPath==null){
            iDisplaySettingView.showUsbAdPicture(null);
        }else if(adPath==""){
            iDisplaySettingView.showUsbAdPicture("");
        }else{//图片存在并且已经复制到app目录
            iDisplaySettingView.showUsbAdPicture(adPath);
        }
    }

}
