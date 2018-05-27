package com.taisau.substation.ui.setting.compare;

/**
 * Created by admin on 2017/8/20.
 */

public interface ICompareSettingModel {
    String getCurrentCompareContent(int flag);//获取本机存储的 显示子菜单 的内容

    void setCompareContentChange(int pos, String content);
    String[] getAgeRange();

    void setAgeRange(String min, String max);
}
