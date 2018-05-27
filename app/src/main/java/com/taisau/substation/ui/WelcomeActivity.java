package com.taisau.substation.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.ui.main.MainActivity;
import com.taisau.substation.util.Preference;


public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initData();
        Logger.d("WelcomeActivity    onCreate");
    }

    private void initData() {
        if (Preference.getServerIp() == null || Preference.getServerIp().equals("")) {
            Preference.setAgeWarning("false");
            Preference.setAliveCheck("false");
            Preference.setScoreRank("easy");
            Preference.setVoiceTips("false");
            Preference.setNoFaceCount("20");
            Preference.setAgeWarningMAX("18");
            Preference.setAgeWarningMIN("0");
            Preference.setServerPort("8899");
            Preference.setGatePort("9010");
            Preference.setFirstTime("0");//初始化App后，连接服务器会发送获取所有名单的命令
            Preference.setDoorway("1");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("WelcomeActivity    onResume");
//                Logger.e("key: "+ GFace.GetSn("TS"));
//        Logger.e("setkey : "+  GFace.SetKey("B052C8C5A91673242EFB"));
        if (getIntent().getStringExtra("skip") != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            Logger.d("WelcomeActivity    onResume     跳过1秒休眠");
            finish();
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }, 1000);
        }
        Logger.d("Devices id = " + getDeviceId(this));
//        泰首身证通主板 Devices id =  null
        //双屏主板 Devices id =  357942051433177

        Logger.d("android id  = " + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        //泰首身证通主板 android id =  83e2ddd7b897422
        //双屏主板 android id =  516fb4abcb6ceb59
    }

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
