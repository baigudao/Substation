package com.taisau.substation.ui.setting.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Administrator on 2017-08-16
 */

public class NetworkSettingPresenter {

    private INetworkSettingView iNetworkSettingView;
    private INetworkSettingModel iNetworkSettingModel;
    private Context context;

    public NetworkSettingPresenter(INetworkSettingView view, Context context) {
        this.iNetworkSettingView = view;
        iNetworkSettingModel=new NetWorkSettingModel(context);
        this.context=context;
    }

    SparseArray<String> getCurrentAddress() {
        SparseArray<String> s = new SparseArray<>();
        try {
          s=iNetworkSettingModel.getCurrentAddress(s);
        } catch (Exception e) {
            e.printStackTrace();
            s.clear();
            s.append(0, "192.168.1.168");
            s.append(1, "255.255.255.0");
            s.append(2, "192.168.1.1");
            s.append(3, "192.168.1.1");
        }
        return s;
    }

    void setAddressChange(int position,String address) {
        try {
            boolean success;
            if(position==0||position==2){//IP
                success = address.matches("((?:(?:25[0-5]|2[0-4]\\d|(?:1\\d{2}|[1-9]?\\d))\\.){3}(?:25[0-5]|2[0-4]\\d|(?:1\\d{2}|[1-9]?\\d)))");
            }else{//端口
                success = address.matches(/*"^\\d{4}$"*/"^[1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5]$ ");
            }
            if(success){
                iNetworkSettingModel.setAddressChange(position,address);
                iNetworkSettingView.showChangeResult(position, address);
            }else{
                iNetworkSettingView.showChangeResult(position, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            iNetworkSettingView.showChangeResult(position, null);
        }
    }
    public int getNetConnectType(){
        //获取网络连接管理者
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        if (networkInfo==null)
            return -2;
        if (networkInfo.getState()== NetworkInfo.State.DISCONNECTED||networkInfo.getState()== NetworkInfo.State.DISCONNECTING)
            return -1;
        Logger.d("net type:"+networkInfo.getTypeName()+" int:"+networkInfo.getType());
        return networkInfo.getType();
    }

}
