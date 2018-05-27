package com.taisau.substation.ui.setting.compare;

import com.orhanobut.logger.Logger;
import com.taisau.substation.util.Preference;

/**
 * Created by admin on 2017/8/20.
 */

public class CompareSettingModel implements ICompareSettingModel {
    private static final String TAG = "CompareSettingModel";
    @Override
    public String getCurrentCompareContent(int flag) {
        String res;
        switch (flag) {
            case 0:
                res = Preference.getBlackWarning();
                break;
            case 1:
                res = Preference.getAliveCheck();
                break;
            case 2:
                res = Preference.getVoiceTips();
                break;
            case 3:
                res = Preference.getAgeWarning();
                break;
            case 4:
                res = Preference.getScoreRank();
                Logger.d( "getCurrentCompareContent:   4 res="+res );
                break;
            case 5:
                res = Preference.getScoreRankValue();
                if(res==null){
                    res = "50";
                }
                Logger.d( "getCurrentCompareContent:   5 res="+res );
                break;
            case 6:
                res = Preference.getWhetherCompare();
                if(res==null){
                    res = "true";
                }
                Logger.d( "getCurrentCompareContent:   6 res="+res );
                break;
            default:
                res = "";
                break;
        }
        return res;
    }


    @Override
    public void setCompareContentChange(int pos, String content) {
        switch (pos) {
            case 0:
                Preference.setBlackWarning(content);
                break;
            case 1:
                Preference.setAliveCheck(content);
                break;
            case 2:
                Preference.setVoiceTips(content);
                break;
            case 3:
                Preference.setAgeWarning(content);
                break;
            case 4:
                Preference.setScoreRank(content);
                Logger.d( "setCompareContentChange:   4 content="+content );
                break;
            case 5:
                Preference.setScoreRankValue(content);
                Logger.e( "setCompareContentChange:   5 content="+content );
                break;
            case 6:
                Preference.setWhetherCompare(content);
                Logger.e( "setCompareContentChange:   6 content="+content );
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getAgeRange() {
        return new String[]{Preference.getAgeWarningMIN(), Preference.getAgeWarningMAX()};
    }

    @Override
    public void setAgeRange(String min, String max) {
        if (min != null && !min.equals("")) {
            Preference.setAgeWarningMIN(min);
        }
        if (max != null && !max.equals("")) {
            Preference.setAgeWarningMAX(max);
        }
    }
}
