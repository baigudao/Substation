package com.taisau.substation.ui.setting.device;

import android.app.Activity;
import android.util.SparseArray;

import com.taisau.substation.util.CityUtils;

import java.util.ArrayList;

/**
 * Created by whx on 2017-08-16
 */

public class DeviceSettingPresenter {
//    private static final String TAG = "DeviceSettingPresenter";
    private IDeviceSettingView iDeviceSettingView;
    private IDeviceSettingModel iDeviceSettingModel;
    private CityUtils cityUtils;
    private String sno;
    public DeviceSettingPresenter(IDeviceSettingView view) {
        this.iDeviceSettingView = view;
        iDeviceSettingModel=new DeviceSettingModel();
        cityUtils =  CityUtils.getInstance(((Activity)view).getApplicationContext());
    }

    public SparseArray<String> getCurrentStatuses() {
        SparseArray<String> statuses = new SparseArray<>();
        try {
            for (int i = 0; i < 8; i++) {
                statuses.append(i, iDeviceSettingModel.getCurrentStatus(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            statuses.clear();
            statuses.append(0, "card001");
            statuses.append(1, "192.168.1.200");
            statuses.append(2, "8080");
            statuses.append(3, "298KB");
            statuses.append(4, "333KB");
            statuses.append(5, "福建");
            statuses.append(6, "泉州");
            statuses.append(7, "南安");
            statuses.append(8, "0");

        }
        for (int i = 0; i <statuses.size() ; i++) {
//            Log.e(TAG, "getCurrentStatuses: "+i+" = "+statuses.get(i));
        }
        return statuses;
    }

    public void setStatusChange(int flag, String value) {
//        Log.e(TAG, "setStatusChange: flag="+flag+",value="+value );
        try {
            iDeviceSettingModel.setStatusChange(flag, value);//若设置失败则model抛异常
            iDeviceSettingView.showChangeResult(flag, value);
        } catch (Exception e) {
            e.printStackTrace();
            iDeviceSettingView.showChangeResult(flag, null);
        }
    }
    public ArrayList<String> getProvincesData(){
        return cityUtils.getProvincesFromJson();
    }
    public ArrayList<String> getCitiesData(String province){
        return cityUtils.getCitiesByProvinceNmae(province);
    }
    public ArrayList<String> getCountriesData(String provinceName, String city){
        return cityUtils.getCountiesByCityName(provinceName,city);
    }
    void releaseCityJson(){
        cityUtils.releaseJsonObject();
        cityUtils = null;
    }
    public void clearList(){
        iDeviceSettingModel.clearList();
    }

    public void clearSoft(){
        iDeviceSettingModel.clearSoft();
    }

    public void checkSerialNum(String num){
        sno = num;
        iDeviceSettingModel.checkSerialNum(num);
    }

}
