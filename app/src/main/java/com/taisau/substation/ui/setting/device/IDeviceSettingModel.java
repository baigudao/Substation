package com.taisau.substation.ui.setting.device;

/**
 * Created by Administrator on 2017-08-16
 */

public interface IDeviceSettingModel {
    /**flag
     * 0: 序列号
     * 1：服务器IP地址
     * 2：服务器端口
     * 3：软件占用存储
     * 4：名单占用存储
     * 5：地址设置,省
     * 6：地址设置，市
     * 7：地址设置，县
     * 8：身份证阅读器类型,RadioButton选中返回"1"，未选中返回"0"
     * */
    String getCurrentStatus(int flag);
    void setStatusChange(int flag, String value);

    void clearSoft();
    void clearList();
    void checkSerialNum( String num);
}
