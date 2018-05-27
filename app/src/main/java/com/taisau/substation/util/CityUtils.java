package com.taisau.substation.util;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by whx on 2017-08-19
 */

public class CityUtils {

    private JSONObject mJsonObj;
    private Context mContext;
    private static CityUtils instance;

    private CityUtils(Context context) {
        this.mContext = context.getApplicationContext();
        initJsonData();
    }

    public static CityUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (CityUtils.class) {
                if (instance == null) {
                    instance = new CityUtils(context);
                }
            }
        }
        return instance;
    }

    /**
     * 从assert文件夹中读取省市区的json文件，然后转化为json对象
     */
    public void initJsonData() {
        try {
            StringBuilder sb = new StringBuilder();
            //city,json放在assets目录下，用assetManager读取不到，换成放在res/raw/下，可以读取了
            InputStream is = mContext.getResources().openRawResource(R.raw.city);
            int len;
            byte[] buf = new byte[1024 * 56];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "UTF-8"));
            }
            buf = null;
            is.close();
            mJsonObj = new JSONObject(sb.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取省数据
     */
    public ArrayList<String> getProvincesFromJson() {
        ArrayList<String> mListProvince = new ArrayList<>();
        try {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 获取每个省的Json对象
                String province = jsonP.getString("name");
                mListProvince.add(province.trim());// 添加省数据
            }
//            Log.e("CityUtils", "getProvincesFromJson: 第一个数据"+ mListProvince.get(0));
        } catch (JSONException | NullPointerException |IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return mListProvince;
    }

    /**
     * 根据省的名字获取对应的城市列表数据
     */
    public ArrayList<String> getCitiesByProvinceNmae(String provinceName) {
        ArrayList<String> mListCity = new ArrayList<>();
        if(provinceName.contains("请选择")){
            mListCity.add("请选择市");
            return  mListCity;
        }
        try {
            mListCity.add("请选择市");
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 获取每个省的Json对象
                String province = jsonP.getString("name");
                if (province.equalsIgnoreCase(provinceName)) {   //查询的省 遍历到了之后，添加城市数据
                    JSONArray jsonCs = jsonP.getJSONArray("city");
                    for (int j = 0; j < jsonCs.length(); j++) {
                        JSONObject jsonC = jsonCs.getJSONObject(j);// 获取每个市的Json对象
                        String city = jsonC.getString("name");
                        mListCity.add(city.trim());// 添加市数据
                    }
                    break;
                }
            }
//            Log.e("CityUtils", "getProvincesFromJson: 第一个数据"+ mListCity.get(0));
        } catch (JSONException | NullPointerException |IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return mListCity;
    }

    /**
     * 根据城市的名字获取对应的县区列表数据
     */
    public ArrayList<String> getCountiesByCityName(String provinceName, String cityName) {
        Logger.e("CityUtils", "getCountiesByCityName: cityName="+cityName );
        ArrayList<String> mListArea = new ArrayList<>();
        if(cityName.contains("请选择")){
            mListArea.add("请选择县");
            return  mListArea;
        }
        try {
            mListArea.add("请选择县");
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 获取每个省的Json对象
                if(!jsonP.getString("name").equals(provinceName)) continue;
                JSONArray jsonCs = jsonP.getJSONArray("city");
                for (int j = 0; j < jsonCs.length(); j++) {
                    JSONObject jsonC = jsonCs.getJSONObject(j);// 获取每个市的Json对象
                    String city = jsonC.getString("name");
                    if (city.equalsIgnoreCase(cityName)) {
                        JSONArray jsonAs = jsonC.getJSONArray("area");
                        for (int k = 0; k < jsonAs.length(); k++) {
                            mListArea.add(jsonAs.getString(k).trim());// 添加区数据
                        }
                        break;
                    }
                }
            }
//            Log.e("CityUtils", "getProvincesFromJson: 第一个数据"+ mListArea.get(0));
        } catch (JSONException | NullPointerException |IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return mListArea;
    }
    public void releaseJsonObject(){
        mJsonObj = null;
        instance = null;
    }
}
