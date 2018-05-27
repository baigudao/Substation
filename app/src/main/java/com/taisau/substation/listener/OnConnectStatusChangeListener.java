package com.taisau.substation.listener;

/**
 * Created by whx on 2017-11-14
 */

public class OnConnectStatusChangeListener {

    private static OnConnectStatusChange listener;

    public static void setOnConnectStatusChangeListener(OnConnectStatusChange onServerIpChange) {
        listener = onServerIpChange;
    }

    public static void OnServerChange(boolean connected) {
        if (listener != null) {
            listener.OnServerChange(connected);
        }
    }
    public static void OnUsbChange(boolean connected) {
        if (listener != null) {
            listener.OnUsbChange(connected);
        }
    }

    public interface OnConnectStatusChange {
        void OnServerChange(boolean connected);
        void OnUsbChange(boolean connected);

    }
}
