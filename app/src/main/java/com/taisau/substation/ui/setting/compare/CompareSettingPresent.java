package com.taisau.substation.ui.setting.compare;


import java.util.ArrayList;

/**
 * Created by admin on 2017/8/20
 */

public class CompareSettingPresent {
    private static final String TAG = "CompareSettingPresent";
    private ICompareSettingModel model;
    private ICompareSettingView view;

    public CompareSettingPresent(ICompareSettingView view)
    {
        this.view = view;
        model=new CompareSettingModel();
    }

    public ArrayList<String> getCompareContent() {
        ArrayList<String> contents = new ArrayList<>();
        try {
            for (int i = 0; i < 7; i++) {
                contents.add(i, model.getCurrentCompareContent(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            contents.clear();
            contents.add("false");
            contents.add("false");
            contents.add("false");
            contents.add("false");
            contents.add("easy");
            contents.add("50");
            contents.add("true");
        }
        return contents;
    }
    public void setCompareContentChange(int position,String content){
        try {
            model.setCompareContentChange(position,content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAgeRange(String min, String max) {
//        Log.e(TAG, "setAgeRange: 保存年龄范围：min="+min+",max="+max );
        model.setAgeRange(min,max);
    }
    /**[min,max]*/
    public void showAgeRange() {
        String[]range=model.getAgeRange();
        if(Integer.valueOf(range[0])> Integer.valueOf(range[1])){
            model.setAgeRange(range[1],range[0]);
            view.showAgeRange(range[1],range[0]);
        }else{
            view.showAgeRange(range[0],range[1]);
        }
    }
}
