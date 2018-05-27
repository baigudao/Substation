package com.taisau.substation.ui.setting.display;

/**
 * Created by whx on 2017-08-17
 */

public interface IDisplaySettingModel {
    /**flag
     * 0:用户名称
     * 1：主标题
     * 2：副标题
    */
    String getCurrentDisplayContent(int flag);//获取本机存储的 显示子菜单 的内容

    void setDisplayContentChange(int pos, String content);
    String uploadPicture(String filePath);//上传广告图片

    String restoreDefaultPicture();//恢复默认图片

    String getUsbAdPicture();//获取U盘的广告图片
}
