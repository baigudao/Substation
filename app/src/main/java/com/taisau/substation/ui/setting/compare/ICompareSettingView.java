package com.taisau.substation.ui.setting.compare;

/**
 * Created by Administrator on 2017-08-17
 */

public interface ICompareSettingView {
    String getCurrentDisplayContent(int flag);
    void setDisplayContentChange(int pos, String content);
//    int[]setAgeRange();

    void  showAgeRange(String min, String max);
}
