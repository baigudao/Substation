package com.taisau.substation.ui.setting.network;

import android.util.SparseArray;

/**
 * Created by Administrator on 2017-08-16
 */

public interface INetworkSettingModel {
    /**flag
     * 0:表示本机IP地址
     * 1：表示子网掩码
     * 2：表示网关
     * 3：表示DNS*/
    SparseArray<String> getCurrentAddress(SparseArray<String> s);
    void setAddressChange(int position,String address);
}
