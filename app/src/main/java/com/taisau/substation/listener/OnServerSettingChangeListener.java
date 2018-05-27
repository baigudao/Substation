package com.taisau.substation.listener;

/**
 * Created by whx on 2017-11-14
 */

public class OnServerSettingChangeListener {

    private static OnServerSettingChange listener;

    public static void setOnServerSettingChangeListener(OnServerSettingChange onServerSettingChange) {
        listener = onServerSettingChange;
    }

    public static void OnSettingChange(String setting) {
        if (listener != null) {
            listener.OnSettingChange(setting);
        }
    }
    public interface OnServerSettingChange {
        void OnSettingChange(String setting);
    }
}
