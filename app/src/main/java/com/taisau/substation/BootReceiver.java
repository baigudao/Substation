package com.taisau.substation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.orhanobut.logger.Logger;
import com.taisau.substation.ui.WelcomeActivity;


/**
 * Created by Administrator on 2018-03-20
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("开机广播");
        new Handler().postDelayed(() -> {
            Intent intent1 = new Intent(context, WelcomeActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
            Logger.d("延时5秒，启动app");
        }, 5000);
    }
}
