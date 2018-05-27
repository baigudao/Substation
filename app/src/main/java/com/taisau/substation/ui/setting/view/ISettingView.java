package com.taisau.substation.ui.setting.view;

import com.taisau.substation.em.DialogCase;

/**
 * Created by whx on 2017-08-15
 */

public interface ISettingView {

    void setRestoreDefaultSuccess();
    void updateAppVersion(String version);
    void setAlertDialogShow(DialogCase dialogCase);
    void toastMsg(String msg);
    void clearDataComplete();
}
