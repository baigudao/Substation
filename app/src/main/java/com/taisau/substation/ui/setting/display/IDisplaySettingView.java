package com.taisau.substation.ui.setting.display;

/**
 * Created by whx on 2017-08-17
 */

public interface IDisplaySettingView {
    void showDialog();
    void hideDialog();
    void showChangeResult(int position, boolean isSuccess);
    void showUploadResult(boolean isSuccess, String result);
    void showRestoreDefaultPictureResult(boolean isSuccess, String result);
    void showUsbAdPicture(String adPath);
}
