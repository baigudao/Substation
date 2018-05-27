package com.taisau.substation.ui.setting.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseArray;

import com.taisau.substation.util.Preference;

import java.io.IOException;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Administrator on 2017/8/18 0018.
 */

public class NetWorkSettingModel implements INetworkSettingModel {

    private static final String TAG = "NetWorkSettingModel";
    NetworkInfo info;
    Context context;

    public NetWorkSettingModel(Context context) {
        this.context = context;
        ConnectivityManager connectionManager = (ConnectivityManager)
                this.context.getSystemService(CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        this.info = connectionManager.getActiveNetworkInfo();
    }


    @Override
    public SparseArray<String> getCurrentAddress(SparseArray<String> s) {
//        if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
//            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//            String ipAddress = intIP2StringIP(dhcpInfo.ipAddress);//得到IPV4地址
//            String netMask= intIP2StringIP(dhcpInfo.netmask);
//            String gateWay= intIP2StringIP(dhcpInfo.gateway);
//            String dns1=intIP2StringIP(dhcpInfo.dns1);
//            s.append(0, ipAddress);
//            s.append(1,netMask);
//            s.append(2,gateWay);
//            s.append(3,dns1);
//        }
//        else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
//
//            try {
//               Runtime.getRuntime().exec("su");    //一直抛异常，显示没有权限
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            String isStop = getAddress("getprop init.svc.dhcpcd_eth0");
//            if(isStop.contains("stop")){//静态地址
//                try{
//                    Process proc= Runtime.getRuntime().exec("ifconfig eth0");
//                    final byte b[] = new byte[1024];
//                    int r = 0;
//                    int i = 0;
//                    String line = "";
//                    final StringBuilder sb = new StringBuilder(line);
//                    while ((r = proc.getInputStream().read(b, 0, 1024)) > -1) {
//                        if (i % 100 == 0) {
//                            sb.delete(0, sb.length());
//                        }
//                        line = new String(b, 0, r);
//                        sb.append(line);
//                    }
//                    int m=line.indexOf("mask");
//                    int f=line.indexOf("flags");
//                    s.append(0,line.substring(9,m-1));//静态ip
//                    s.append(1,line.substring(m+5,f-1));//mask
//                    s.append(2,"192.168.2.1");//TODO ip route show 无法获取准确网关
//                    s.append(3,getAddress("getprop net.dns1"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }else{
//                ArrayList<String> commands = new ArrayList<>();
//                commands.add("getprop dhcp.eth0.ipaddress");
//                commands.add("getprop dhcp.eth0.mask");//掩码
//                commands.add("getprop dhcp.eth0.gateway");//网关
//                commands.add("getprop dhcp.eth0.dns1");//DNS
//                for (int i = 0; i <commands.size() ; i++) {
//                    s.append(i,getAddress(commands.get(i)));
//                }
//            }
//
//        }
        s.append(0, Preference.getServerIp());//ip
        s.append(1, Preference.getServerPort());//端口
        s.append(2, Preference.getGateIp());//gate ip
        s.append(3, Preference.getGatePort());//gate 端口
        return s;
    }

    @Override
    public void setAddressChange(int position, String address) {
        switch (position) {
            case 0:
                Preference.setServerIp(address);
                break;
            case 1:
                Preference.setServerPort(address);
                break;
            case 2:
                Preference.setGateIp(address);
                break;
            case 3:
                Preference.setGatePort(address);
                break;
            default:
                break;
        }
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    private String getAddress(String command) {
        StringBuilder sb = new StringBuilder("");
        try {
            Process proc = Runtime.getRuntime().exec(command);
            final byte b[] = new byte[1024];
            int r;
            String line;
            while ((r = proc.getInputStream().read(b, 0, 1024)) > -1) {
                line = new String(b, 0, r);
                sb.append(line);
            }
//            Log.e(TAG, "getAddress: command="+command+",sb" + sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }
}
