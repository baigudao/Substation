package com.taisau.substation.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.serialport.api.SerialPort;
import android.text.format.DateFormat;
import android.util.Base64;
import android.widget.Toast;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.SubstationApplication;
import com.taisau.substation.bean.History;
import com.taisau.substation.bean.HistoryDao;
import com.taisau.substation.bean.MsgUseElectricRecord;
import com.taisau.substation.bean.MsgUseElectricRecordDao;
import com.taisau.substation.bean.Person;
import com.taisau.substation.bean.PersonDao;
import com.taisau.substation.listener.OnCardDetectListener;
import com.taisau.substation.listener.OnConnectStatusChangeListener;
import com.taisau.substation.listener.OnServerSettingChangeListener;
import com.taisau.substation.listener.OnUpdateScoreListener;
import com.taisau.substation.listener.SaveAndUpload;
import com.taisau.substation.ui.main.MainActivity;
import com.taisau.substation.ui.main.model.MainModel;
import com.taisau.substation.util.Constant;
import com.taisau.substation.util.FeaUtils;
import com.taisau.substation.util.FileUtils;
import com.taisau.substation.util.ImgUtils;
import com.taisau.substation.util.Preference;
import com.taisau.substation.util.ThreadPoolUtils;
import com.taisau.substation.util.XmlUtil;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import static com.taisau.substation.util.Constant.TEMPLATE_FEA;
import static com.taisau.substation.util.Constant.TEMPLATE_IMG;


public class WhiteListService extends Service implements SaveAndUpload.OnSaveAndUploadListener, OnServerSettingChangeListener.OnServerSettingChange {

    private final WhiteListBinder binder = new WhiteListBinder();
    private Socket socket;
    private boolean isRun;
    private Handler handler = new Handler();

    private final int MSG_CMD = 2;
    private final int NET_COMMAND_UNKNOW = -1;

    //命令
    private static final int NET_UPLOAD_CARDINFO = 0x0000;                //上传身份证信息
    private static final int NET_UPLOAD_CARDINFO_BACK = 0x0001;           //上传身份证信息的返回
    private static final int NET_UPLOAD_RECORD = 0x0002;                  //上传比对记录
    private static final int NET_UPLOAD_RECORD_BACK = 0x0003;              //上传比对记录的返回
    private static final int NET_UPLOAD_CONFIG = 0x0004;                   //上传闸机配置
    private static final int NET_UPLOAD_CONFIG_BACK = 0x0005;               //上传闸机配置的返回
    //    private static final int NET_CHECK_IDCARD = 0x0006;      //不需要    //上传检测身份证信息是否已存在
//    private static final int NET_CHECK_IDCARD_BACK = 0x0007; //不需要   //上传检测身份证信息是否已存在的返回
    private static final int NET_KEEPLIVE = 0x0008;                        //心跳
    private static final int NET_DOWNLOAD_CONTROL = 0x0009;                  //下发控制命令
    private static final int NET_DOWNLOAD_CONFIG = 0x000a;                  //下发闸机配置        设备收到下发配置后，直接再上传一次配置
    //    private static final int NET_DOWNLOAD_CONFIG_BACK = 0x000b;              //下发闸机配置的返回   不需要返回，直接上传一次配置信息
    private static final int NET_DOWNLOAD_OPENDOOR = 0x000c;                 //下发开门命令
    private static final int NET_DOWNLOAD_ROSTER_INFO_UPDATE = 0x000d;        //下发名单信息更新（新增、修改）
    private static final int NET_DOWNLOAD_ROSTER_PHOTO_UPDATE = 0x000e;      //下发名单照片更新（新增、修改）
    private static final int NET_DOWNLOAD_ROSTER_REMOVE = 0x000f;           //下发名单移除
    private static final int NET_DOWNLOAD_ROSTER_CLEAN = 0x0010;             //下发名单清空
    private static final int NET_DOWNLOAD_ROSTER_OPERATE_BACK = 0x0011;     //下发名单操作的返回
    private static final int NET_UPLOAD_STRANGER_RECORD = 0x0012;   //上传陌生人记录
    private static final int NET_UPLOAD_STRANGER_RECORD_BACK = 0x0013;   //上传陌生人记录的返回

    private static final int NET_UPLOAD_USEELECTRIC_RECORD = 0x0016;//上传用电记录
    private static final int NET_UPLOAD_USEELECTRIC_RECORD_BACK = 0x0017;//上传用电记录的返回

    //比对结果类型
    private static final int EDR_UNKNOW = -1;         //
    private static final int EDR_SUCCESS = 0;               //成功
    private static final int EDR_FAILED = 1;               //失败
    private static final int EDR_REGISTER = 2;         //提示登记
    //比对类型
    private static final int ECT_IDCARD_FACE = 0;        //刷卡人脸比对
    private static final int ECT_ROSTER_FACE = 1;        //名单人脸比对
    private static final int ECM_ICCARD_FACE = 2;        //IC卡人脸比对
    private static final int ECM_QRCODE_FACE = 3;       //二维码人脸比对

    //通行模式
    private static final int EPM_Gated = 0;    //门禁模式
    private static final int EPM_Visitor = 1;    //访客模式

    //配电箱輸出指令
    private static final String CHECK_EIGHT_STATUS = "FE 01 00 00 00 08 29 C3";//查询八路状态
    private static final String CONTROL_OPEN_1 = "FE 05 00 00 FF 00 98 35";//控制第一路开
    private static final String CONTROL_CLOSE_1 = "FE 05 00 00 00 00 D9 C5";//控制第一路关
    private static final String CONTROL_OPEN_2 = "FE 05 00 01 FF 00 C9 F5";//控制第二路开
    private static final String CONTROL_CLOSE_2 = "FE 05 00 01 00 00 88 05";//控制第二路关
    private static final String CONTROL_OPEN_3 = "FE 05 00 02 FF 00 39 F5";//控制第三路开
    private static final String CONTROL_CLOSE_3 = "FE 05 00 02 00 00 78 05";//控制第三路关
    private static final String CONTROL_OPEN_4 = "FE 05 00 03 FF 00 68 35";//控制第四路开
    private static final String CONTROL_CLOSE_4 = "FE 05 00 03 00 00 29 C5";//控制第四路关
    private static final String CONTROL_OPEN_5 = "FE 05 00 04 FF 00 D9 F4";//控制第五路开
    private static final String CONTROL_CLOSE_5 = "FE 05 00 04 00 00 98 04";//控制第五路关
    private static final String CONTROL_OPEN_6 = "FE 05 00 05 FF 00 88 34";//控制第六路开
    private static final String CONTROL_CLOSE_6 = "FE 05 00 05 00 00 C9 C4";//控制第六路关
    private static final String CONTROL_OPEN_7 = "FE 05 00 06 FF 00 78 34";//控制第七路开
    private static final String CONTROL_CLOSE_7 = "FE 05 00 06 00 00 39 C4";//控制第七路关
    private static final String CONTROL_OPEN_8 = "FE 05 00 07 FF 00 29 F4";//控制第八路开
    private static final String CONTROL_CLOSE_8 = "FE 05 00 07 00 00 68 04";//控制第八路关
    private static final String CONTROL_OPEN_ALL = "FE 0F 00 00 00 08 01 FF F1 D1";//控制全开
    private static final String CONTROL_CLOSE_ALL = "FE 0F 00 00 00 08 01 00 B1 91";//控制全关
    private static final String CHECK_EIGHT_OU_STATUS = "FE 02 00 00 00 08 6D C3";//查询八路光耦状态

    private boolean isOpen_1 = false;
    private boolean isOpen_2 = false;
    private boolean isOpen_3 = false;
    private boolean isOpen_4 = false;
    private boolean isOpen_5 = false;
    private boolean isOpen_6 = false;
    private boolean isOpen_7 = false;
    private boolean isOpen_8 = false;


//    String sn = android.os.Build.SERIAL; //当前设备为：P1QRMBUAKN  。设备序列号，双屏设备返回unknown

    String sn = Settings.Secure.getString(SubstationApplication.getApplication().getContentResolver(), Settings.Secure.ANDROID_ID); //android id


    private List<Byte> queue = new LinkedList<>();
    private volatile List<String> modifyPersonList = new ArrayList<>();
    private volatile List<String> modifyPersonPhotoList = new ArrayList<>();
    private volatile boolean isPersonSaving = false;
    private volatile boolean isPersonPhotoSaving = false;
    private volatile Future<?> keepAliveFuture, reuploadThread, openTcpFuture;
    private volatile long currentTime;
    public volatile static boolean isTcpConnected = false;
    //    private volatile CopyOnWriteArrayList<Upload> uploadList = new CopyOnWriteArrayList<>();
    private NfcReceiver nfcReceiver;
    private static final String ACTION_NFC_RECEIVER_CARD_NUM = "hs.android.NfcAdapte.ACTION_NDEF_DISCOVERED";

    public WhiteListService() {
    }

    public class WhiteListBinder extends Binder {
        public WhiteListService getService() {
            return WhiteListService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //设置保存和上传监听
        SaveAndUpload.setOnSaveAndUploadListener(this);
        //设置服务改变的监听
        OnServerSettingChangeListener.setOnServerSettingChangeListener(this);
        //打开tcp
        openTcp();
        //注册广播接收器
        IntentFilter filter = new IntentFilter(ACTION_NFC_RECEIVER_CARD_NUM);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        nfcReceiver = new NfcReceiver();
        registerReceiver(nfcReceiver, filter);
        //打开串口
        ThreadPoolUtils.execute(this::OpenSerial);
        ThreadPoolUtils.execute(this::OpenAnotherSerial);//打开另一个串口（如果有两个不同串口的话）
//        Preference.setServerIp("192.168.2.118");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SaveAndUpload.setOnSaveAndUploadListener(null);
        OnServerSettingChangeListener.setOnServerSettingChangeListener(null);
        closeTcp();
        if (openTcpFuture != null && (!openTcpFuture.isCancelled() || openTcpFuture.isDone())) {
            openTcpFuture.cancel(true);
            Logger.i("reuploadThread   " + openTcpFuture.isCancelled());
            openTcpFuture = null;
        }
        unregisterReceiver(nfcReceiver);
        closeSerialPort();
        closeAnotherSerialPort();
        return super.onUnbind(intent);
    }

    //如果没连接成功，反复提交任务去连接服务
    private void openTcp() {
//        Logger.d("openTcp。。。。。。。");
        if (isTcpConnected)
            return;
//        if (openTcpFuture != null && !openTcpFuture.isDone()) {
//            openTcpFuture.cancel(true);
//        }
        openTcpFuture = ThreadPoolUtils.submit(() -> {
            try {
                String ip = Preference.getServerIp();
//                Logger.e("服务的IP：" + ip);
                if (ip == null || ip.equals("")) {
//                handler.post(() -> Toast.makeText(WhiteListService.this, R.string.no_ip, Toast.LENGTH_LONG).show());
                    Thread.sleep(10000);
                    openTcp();
                } else {
                    String portStr = Preference.getServerPort();
                    int port;
                    if (portStr == null || portStr.equals("")) {
                        port = 8899;
                    } else {
                        port = Integer.valueOf(portStr);
                    }
                    socket = new Socket(ip, port);
                    if (!socket.isConnected()) {
                        Logger.d(" 服务器    socket   no connect");
                        Thread.sleep(10000);
                        openTcp();
                    } else {
                        isTcpConnected = true;
                        Logger.d("serial number = " + sn);
                        uploadConfig();//更新配置
                        startRead();//开始读取数据
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
//                handler.post(() -> Toast.makeText(WhiteListService.this,
//                        R.string.tips_connect_exception, Toast.LENGTH_SHORT).show());
                try {
                    closeTcp();
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                openTcp();
            }
        });
    }

    //    private int startCount = 0;
    private void startRead() {
//        Logger.d("startRead。。。。。。。。。。。");
        if (isRun) {
            Logger.e(" 多线程开启TCP  startRead，return");
            return;
        }
//        startCount++;
//        Logger.d("startRead      开启次数  startCount =" + startCount);
        isRun = true;
        InputStream mInStream = null;
        BufferedInputStream bis;
        try {
            mInStream = socket.getInputStream();
            int SIZE = 1024 * 32;
            bis = new BufferedInputStream(mInStream, SIZE);
            byte[] buffer = new byte[SIZE];
            int tmp;
            while (isRun) {
                tmp = bis.read(buffer);
                currentTime = System.currentTimeMillis();
//                Logger.d("1    queue.size()=" + queue.size());
                for (int i = 0; i < tmp; i++) {
                    queue.add(buffer[i]);
                }
//                Logger.d("tmp=" + tmp);
                if (queue.size() >= 19) {
                    checkMsg();
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            MainModel.runDetect = false;//long true
            ThreadPoolUtils.execute(() -> {
                try {
                    closeTcp();
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                openTcp();
            });
        } finally {
            if (mInStream != null) {
                try {
                    mInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //只要服务连接成功，这个方法会循环执行，间隔5秒
    private void checkMsg() {
//        Logger.d("checkMsg。。。。。。。。。。。");
        try {
            byte[] front = new byte[19];
            try {
                for (int i = 0; i < front.length; i++) {
                    front[i] = queue.get(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("queue = " + Arrays.toString(queue.toArray()));
                Logger.e("queue.size = " + queue.size());
                return;
            }
//            Logger.d("2    queue.size()=" + queue.size());
            if (isHeadMatch(front, "TS_HEAD".getBytes())) {
                byte[] contentLength = new byte[4];//7~10
                System.arraycopy(front, 7, contentLength, 0, contentLength.length);
                int contentLengthInt = bytesToInt(contentLength, 0);
                byte[] msgType = new byte[4];//11~14
                System.arraycopy(front, 11, msgType, 0, msgType.length);
                int msgTypeInt = bytesToInt(msgType, 0);
                byte[] cmdType = new byte[4];//15~18
                System.arraycopy(front, 15, cmdType, 0, cmdType.length);
                int cmdTypeInt = bytesToInt(cmdType, 0);
                byte[] content = new byte[contentLengthInt]; //下发的内容
                if (queue.size() < 19 + contentLengthInt + 7) {//接收到的内容是被截掉的
                    Logger.d("消息不完整，回去添加,return");
                    return;
                } else {
                    try {
                        for (int i = 0; i < contentLengthInt; i++) {
                            content[i] = queue.get(i + 19);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("contentLengthInt = " + contentLengthInt);
                        Logger.e("content.length = " + content.length);
                        Logger.e("queue.size = " + queue.size());
                        return;
                    }
                }
//                System.arraycopy(front, 19, content, 0, contentLengthInt);
//                Logger.d("消息完整");
//                Logger.d("3    queue.size()=" + queue.size());
                for (int i = 0; i < 19 + contentLengthInt + 7; i++) {
                    queue.remove(0);
                }
//                Logger.d("4    queue.size()=" + queue.size());
                String contentStr = new String(content, "UTF-8");
//                Logger.d("contentStr=" + contentStr);

                handleMsg(msgTypeInt, cmdTypeInt, contentStr);
            } else {//内容被截断，queue里面不包含完整消息
//                Logger.e(":截取Queue前18个字节，不包含消息头 ");
                for (int i = 0; i < queue.size(); i++) {
                    if (null == queue.get(i)) {
                        queue.remove(0);
                    } else {
                        break;
                    }
                }
            }
            if (queue.size() != 0) {
//                Logger.d("消息处理完后，列表不为空，递归调用处理消息");
                checkMsg();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            MainModel.runDetect = false;//long true
        }
    }

    /**
     * 根据 消息类型 和 命令类型 处理消息
     */
    private void handleMsg(int msgTypeInt, int cmdTypeInt, String contentStr) {
        if (msgTypeInt == MSG_CMD) {
//            Logger.d(cmdTypeInt);
            switch (cmdTypeInt) {
                case NET_DOWNLOAD_ROSTER_CLEAN://清空下发的人员
                    Logger.d("NET_DOWNLOAD_ROSTER_CLEAN");
                    try {
                        JSONObject object = new JSONObject(contentStr);
                        String operateTime = object.getString("OperateTime");
                        cleanPerson(operateTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case NET_DOWNLOAD_ROSTER_INFO_UPDATE://更新下发id的人员，不包含图片
                    Logger.d("NET_DOWNLOAD_ROSTER_INFO_UPDATE");
                    modifyPersonList.add(contentStr);
                    if (!isPersonSaving) {
                        isPersonSaving = true;
                        MainModel.runDetect = false;
                        savePerson();
                    }
                    break;
                case NET_DOWNLOAD_ROSTER_PHOTO_UPDATE://更新人员的图片
                    Logger.d("NET_DOWNLOAD_ROSTER_PHOTO_UPDATE");
                    modifyPersonPhotoList.add(contentStr);
                    if (!isPersonPhotoSaving) {
                        isPersonPhotoSaving = true;
                        MainModel.runDetect = false;
                        savePersonPhoto();
                    }
                    break;
                case NET_DOWNLOAD_ROSTER_REMOVE://移除 下发的id的人员
                    Logger.d("NET_DOWNLOAD_ROSTER_REMOVE");
                    try {
                        JSONObject object = new JSONObject(contentStr);
                        String uid = object.getString("Uid");
                        String operateTime = object.getString("OperateTime");
                        removePerson(uid, operateTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case NET_UPLOAD_CONFIG_BACK://上线发送sn信息后收到这个回复
                    Logger.d("NET_UPLOAD_CONFIG_BACK");
                    try {
                        JSONObject object = new JSONObject(contentStr);
                        String sn = object.getString("Serial");
                        boolean result = object.getBoolean("Result");
                        Logger.d("NET_UPLOAD_CONFIG_BACK sn=" + sn + ",result = " + result);
//                        reuploadHistory();
                        reUploadUseElectricRecord();
                        startKeepAliveThread();//開啟心跳包
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case NET_KEEPLIVE://心跳包，每隔5秒，设备发给服务器，服务器原样返回
//                           Logger.d("NET_KEEPLIVE");
//                    try {
//                        JSONObject object = new JSONObject(contentStr);
//                        String sn = object.getString("Serial");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    break;
                case NET_UPLOAD_RECORD_BACK://上传比对记录后，返回
                    Logger.d("NET_UPLOAD_RECORD_BACK");
                    try {
                        JSONObject object = new JSONObject(contentStr);
                        String uid = object.getString("Uid");
                        String createTime = object.getString("CreateTime");
                        boolean result = object.getBoolean("Result");
                        Logger.d("NET_UPLOAD_RECORD_BACK    Uid=" + uid + ",createTime="
                                + createTime + ",Result=" + result);
                        uploadSuccess(uid, createTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case NET_UPLOAD_USEELECTRIC_RECORD_BACK:
                    Logger.d("NET_UPLOAD_USEELECTRIC_RECORD_BACK");
                    try {
                        JSONObject object = new JSONObject(contentStr);
                        String uid = object.getString("Uid");
                        String createTime = object.getString("CreateTime");
                        Logger.d("NET_UPLOAD_USEELECTRIC_RECORD_BACK    Uid=" + uid + ",createTime=" + createTime);
                        uploadSuccess(uid, createTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case NET_DOWNLOAD_CONFIG://下发配置
                    Logger.d("NET_DOWNLOAD_CONFIG");
                    try {
                        JSONObject object = new JSONObject(contentStr);
//                        String channel = object.getString("Channel");
                        String deviceName = object.getString("DeviceName");
                        Preference.setCustomName(deviceName);
                        String detectScore = object.getString("DetectScore");
                        Preference.setOvoCompareThreshold(detectScore);//人证比对阈值，即一比一比对

                        if (onUpdateScoreListener != null) {
                            onUpdateScoreListener.onUpdateScore(Float.valueOf(detectScore));
                        }
                        String doorway = object.getString("InOut");//出入口，1为入口 2为出口
                        Preference.setDoorway(doorway);//
//                        String wlDetectThreshold = object.getString("WLDetectThreshold");
//                        Preference.setCompareThreshold(wlDetectThreshold);//自动比对阈值，即刷脸白名单比对
                        uploadConfig();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        uploadConfig();
                    }
                    break;
                case NET_DOWNLOAD_OPENDOOR:
                    Logger.d("NET_DOWNLOAD_OPENDOOR");
                    try {
                        JSONObject object = new JSONObject(contentStr);
                        String channel = object.getString("Channel");//闸机通道，也就是设备序列号
                        if (channel.equals(sn)) {
                            String prot = "ttyS3";
                            int baudrate = 9600;
                            SerialPort mSerialPort = new SerialPort(new File("/dev/" + prot), baudrate, 0);
                            Logger.d("  串口打开        " + mSerialPort.toString());
                            OutputStream mOutputStream = mSerialPort.getOutputStream();
                            mOutputStream.write((byte) 0xf0);
                            Logger.i("发送成功:0xf0");
                            //开一秒，立马发送关闭指令，有人在闸机中间的话，闸机有红外感应，不会夹到人
                            Thread.sleep(1000);
                            mOutputStream.write((byte) 0x00);
                        }
                    } catch (JSONException | InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case NET_DOWNLOAD_CONTROL:
                    Logger.d("NET_DOWNLOAD_CONTROL");
//                    try {
//                        JSONObject object = new JSONObject(contentStr);
//                        int controlNum = object.getInt("ControlNum");//1关机 2重启设备
//                        switch (controlNum) {
//                            case 1:
//                                PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                                pManager.reboot(null);//重启
//                                break;
//                            case 2:
//                                //获得ServiceManager类
//                                Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
//
//                                //获得ServiceManager的getService方法
//                                Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
//
//                                //调用getService获取RemoteService
//                                Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);
//
//                                //获得IPowerManager.Stub类
//                                Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
//                                //获得asInterface方法
//                                Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
//                                //调用asInterface方法获取IPowerManager对象
//                                Object oIPowerManager = asInterface.invoke(null, oRemoteService);
//                                //获得shutdown()方法
//                                Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class, boolean.class);
//                                //调用shutdown()方法
//                                shutdown.invoke(oIPowerManager, false, true);
//                                break;
//                        }
//                    } catch (JSONException | NoSuchMethodException | IllegalAccessException
//                            | ClassNotFoundException | InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
                    break;

            }
        }
    }

    @Override
    public void OnSaveAndUpload(final long historyId, final Bitmap face, final String card_path,
                                final String face_path, final String com_status, final float score,
                                final String time, final String registerId,
                                final int result, final int compareType) { //在这里保存历史记录，然后上传
//        uploadList.add(upload);
//        Logger.d("上传列表大小：" + uploadList.size());
//        uploadHistory
//(uploadList.get(0));
        ThreadPoolUtils.execute(() -> {
            try {
                Logger.d("hisID:" + historyId);
                ImgUtils.getUtils().saveBitmap(face, face_path);
                History history = new History();
                history.setId(historyId);
                history.setIc_card(registerId);
                history.setTemplatePhotoPath(card_path);
                history.setFace_path(face_path);
                history.setTime(time);
                history.setCom_status(com_status);
                history.setScore(score);
                history.setResult(result);
                history.setCompareType(compareType);
                Logger.e("Integer.parseInt(Preference.getDoorway()) = " + Integer.parseInt(Preference.getDoorway()));
                history.setInOut(Integer.parseInt(Preference.getDoorway()));  //出入口   1为入口  2为出口

                if (SubstationApplication.getApplication().getDaoSession().getHistoryDao().count() >= 10000) {
                    try {
                        if (Preference.getHisFirstIdId() != null && !Preference.getHisFirstIdId().equals("")) {
                            long firstID = Long.parseLong(Preference.getHisFirstIdId());
                            History first = SubstationApplication.getApplication().getDaoSession().getHistoryDao().load(firstID);
                            FileUtils.deleteFile(first.getFace_path());
                            FileUtils.deleteFile(first.getTemplatePhotoPath());
                            SubstationApplication.getApplication().getDaoSession().delete(first);
                            firstID++;
                            Preference.setHisFirstId("" + firstID);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                Preference.setHisLastId("" + historyId);
                SubstationApplication.getApplication().getDaoSession().insert(history);
                uploadHistory(history);
                Logger.d("save history = " + history.toString());
//                personCache = null;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void OnUploadIdentityCard(String[] info) {
    }

    @Override
    public void OnSettingChange(final String setting) {
        Logger.d("与服务器相关的设置 发生改变 = " + setting);
        ThreadPoolUtils.execute(() -> {
            if (setting.contains("name")) {//改设备位置
                uploadConfig();
            } else {//改ip或端口
                closeTcp();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                openTcp();
            }

        });

    }

    private void uploadConfig() {
        List<String> msgKeyList = new ArrayList<>();
        List<Object> msgValueList = new ArrayList<>();
        msgKeyList.add("Serial");//设备序列号
        msgKeyList.add("InstallSite");//安装地址
        msgKeyList.add("DeviceName");//安装地址
        msgKeyList.add("DetectScore");//人证比对阈值
        msgKeyList.add("WLDetectThreshold");//员工自动比对阈值
        msgKeyList.add("InOut");//出入口，1为入口 2为出口
        msgValueList.add(sn);
        String site = Preference.getCustomName() == null ? "未设置" : Preference.getCustomName();
        msgValueList.add(site);
        msgValueList.add(site);
        msgValueList.add(Preference.getOvoCompareThreshold() == null ? "85" : Preference.getOvoCompareThreshold());
        msgValueList.add(Preference.getCompareThreshold() == null ? "85" : Preference.getCompareThreshold());
        msgValueList.add(Preference.getDoorway());
        sendMsg2(msgKeyList, msgValueList, NET_UPLOAD_CONFIG);
    }

    private static OnUpdateScoreListener onUpdateScoreListener;

    public static void setOnUpdateScoreListener(OnUpdateScoreListener onUpdateScoreListener) {
        WhiteListService.onUpdateScoreListener = onUpdateScoreListener;
    }

    //心跳包
    private void startKeepAliveThread() {
        if (keepAliveFuture == null || (keepAliveFuture.isDone() || keepAliveFuture.isCancelled())) {
            keepAliveFuture = ThreadPoolUtils.submit(() -> {
                while (isRun) {
                    long time = System.currentTimeMillis();
//                   Logger.d(time - currentTime);
                    if (time - currentTime < 30000) {
                        List<String> msgKeyList = new ArrayList<>();
                        List<Object> msgValueList = new ArrayList<>();
                        msgKeyList.add("Serial");
                        msgValueList.add(sn);
                        sendMsg2(msgKeyList, msgValueList, NET_KEEPLIVE);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Logger.d("心跳包未收到回复，关闭连接，重新连接");
                        closeTcp();
                        ThreadPoolUtils.execute(() -> {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            openTcp();
                        });

                    }
                }
            });
        }
    }

    public void reOpenTcp() {
        ThreadPoolUtils.execute(() -> {
            closeTcp();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openTcp();
        });
    }

    private void closeTcp() {
        isRun = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        queue.clear();
        if (keepAliveFuture != null && (!keepAliveFuture.isCancelled() || keepAliveFuture.isDone())) {
            keepAliveFuture.cancel(true);
//            Logger.i("keepAliveFuture   " + keepAliveFuture.isCancelled());
            keepAliveFuture = null;
        }
        if (reuploadThread != null && (!reuploadThread.isCancelled() || reuploadThread.isDone())) {
            reuploadThread.cancel(true);
//            Logger.i("reuploadThread   " + reuploadThread.isCancelled());
            reuploadThread = null;
        }
        isTcpConnected = false;
        OnConnectStatusChangeListener.OnServerChange(false);
    }

    private void baseSendMsg(final JSONObject jsonObject, final int comType) {
        synchronized (this) {
            if (socket != null) {
                try {
                    byte[] MsgHead = "TS_HEAD".getBytes();
                    byte[] len = intToBytes(jsonObject.toString().getBytes().length);
                    byte[] EMESSAGETYPE = intToBytes(MSG_CMD);
                    byte[] ECOMMANDTYPE = intToBytes(comType);
                    byte[] Info = jsonObject.toString().getBytes();
                    byte[] MsgTail = "TS_TAIL".getBytes();
                    byte[] b1 = byteMerger(MsgHead, len);
                    byte[] b2 = byteMerger(EMESSAGETYPE, ECOMMANDTYPE);
                    byte[] b3 = byteMerger(Info, MsgTail);
                    byte[] b4 = byteMerger(b1, b2);
                    byte[] b5 = byteMerger(b4, b3);
                    socket.getOutputStream().write(b5);
                    OnConnectStatusChangeListener.OnServerChange(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    OnConnectStatusChangeListener.OnServerChange(false);
                    isTcpConnected = false;
                }
            }
        }
    }

    private void sendMsg2(List<String> msgKeyList, List<?> msgValueList, final int comType) {
        if (socket != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                for (int i = 0; i < msgKeyList.size(); i++) {
                    jsonObject.put(msgKeyList.get(i), msgValueList.get(i));
                }
                baseSendMsg(jsonObject, comType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Person person;

    private void savePerson() {
        ThreadPoolUtils.execute(() -> {
            String operateTime = null;
            try {
                while (modifyPersonList.size() != 0) {
                    Logger.d("modifyPersonList.size()=" + modifyPersonList.size());
                    JSONObject object = new JSONObject(modifyPersonList.get(0));
                    //操作时间，暂时没定义，需要再补
                    operateTime = object.getString("OperateTime");
                    String uid = object.getString("Uid");
                    String id = object.getString("CIc");
//                    String icCard = object.getString("ic_card");
                    String enterDate = object.getString("EnterDate");//受雇日期
                    String leaveDate = object.getString("LeaveDate");//离职日期
                    String siteBeginDate = object.getString("SiteBeginDate");//地点开始日期
                    String siteEndDate = object.getString("SiteEndDate");//地点结束日期
                    String safetyCardExpiryDate = object.getString("SafetyCardExpiryDate");//安全卡到期日期

                    QueryBuilder<Person> qb = SubstationApplication.getApplication().getDaoSession()
                            .getPersonDao().queryBuilder().where(PersonDao.Properties.Uid.eq(uid));
                    if (qb.list().size() != 0) {
                        Logger.d("下发人员重复，更新  id=" + id);
                        person = qb.list().get(0);
                    } else {
                        person = new Person();
                    }
                    person.setIc_card(id);
                    person.setUid(uid);
                    person.setEnterDate(enterDate);
                    person.setLeaveDate(leaveDate);
                    person.setSiteBeginDate(siteBeginDate);
                    person.setSiteEndDate(siteEndDate);
                    person.setSafetyCardExpiryDate(safetyCardExpiryDate);

                    SubstationApplication.getApplication().getDaoSession().getPersonDao().insertOrReplace(person);
                    Logger.d("下发人员，添加成功  id = " + id);
                    handler.post(() -> Toast.makeText(WhiteListService.this, R.string.person_save_success, Toast.LENGTH_SHORT).show());
                    modifyPersonList.remove(0);
                    modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_INFO_UPDATE, operateTime, true);
                }
            } catch (JSONException | NullPointerException e) {
                Logger.d("下发人员，添加异常");
                e.printStackTrace();
                modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_INFO_UPDATE, operateTime, false);
            } finally {
                isPersonSaving = false;
                MainModel.runDetect = false;//long true
            }
        });
    }

    private void savePersonPhoto() {
        ThreadPoolUtils.execute(() -> {
            try {
                while (modifyPersonPhotoList.size() != 0) {
                    Logger.d("modifyPersonPhotoList.size()=" + modifyPersonPhotoList.size());

                    JSONObject object = new JSONObject(modifyPersonPhotoList.get(0));
                    String operateTime = object.getString("OperateTime");
                    String uid = object.getString("Uid");
                    int photoIndex = object.getInt("PhotoIndex");
                    Logger.d("下发人员  图片 uid = +" + uid + ",photoIndex = " + photoIndex);
                    String photo = object.getString("Photo");
                    int flag; //增加、修改、删除分别为：0、1、2
                    QueryBuilder<Person> qb = SubstationApplication.getApplication().getDaoSession()
                            .getPersonDao().queryBuilder().where(PersonDao.Properties.Uid.eq(uid));
                    if (qb.list().size() != 0) {
                        Logger.d("下发人员  图片   人员已存在，更新");
                        person = qb.list().get(0);
                        flag = 1;
                    } else {
                        person = new Person();
                        flag = 0;
                    }

                    Bitmap bitmap = base64ToBitmap(photo);
                    File file;
                    person.setUid(uid);
                    person.setIc_card(uid);

                    String path = TEMPLATE_IMG + "/" + DateFormat.format("yyyyMMdd_HHmmss",
                            Calendar.getInstance(Locale.CHINA)) + "_img.jpg";
                    file = new File(path);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, 400, 600, false);
                    ImgUtils.getUtils().saveBitmap(bitmap, path);
                    float bitFea[] = ImgUtils.getUtils().getImgFea(bitmap);
                    int errorCount = 0;
                    if (bitFea == null) {
                        while (bitFea == null && errorCount < 3) {
                            bitFea = ImgUtils.getUtils().getImgFea(bitmap);
                            errorCount++;
                            Logger.d("               下发     人员图片，提取特征值失败次数：" + errorCount);
                            Thread.sleep(500);
                        }
                    }
                    if (bitFea != null) {
                        String whiteFea = TEMPLATE_FEA + "/" + DateFormat.format("yyyyMMdd_HHmmss",
                                Calendar.getInstance(Locale.CHINA)) + "_fea.txt";

                        File photoFile = null;
                        //获取到图片编号后，根据编号去删除原有的图片，然后用新的路径替换,特征值路径固定，直接替换
                        switch (photoIndex) {
                            case 0:
                                if (flag == 1) {//增加、修改、删除分别为：0、1、2
                                    if (person.getImg_path() != null) {
                                        photoFile = new File(person.getImg_path());
                                        whiteFea = person.getFea_path();
                                    }
                                }
                                person.setFea_path(whiteFea);
                                person.setImg_path(path);
                                break;
                            case 1:
                                if (flag == 1) {//增加、修改、删除分别为：0、1、2
                                    if (person.getPush_img_path1() != null) {
                                        photoFile = new File(person.getPush_img_path1());
                                        whiteFea = person.getPush_fea_path1();
                                    }
                                }
                                person.setPush_fea_path1(whiteFea);
                                person.setPush_img_path1(path);
                                break;
                            case 2:
                                if (flag == 1) {//增加、修改、删除分别为：0、1、2
                                    if (person.getPush_img_path2() != null) {
                                        photoFile = new File(person.getPush_img_path2());
                                        whiteFea = person.getPush_fea_path2();
                                    }
                                }
                                person.setPush_fea_path2(whiteFea);
                                person.setPush_img_path2(path);
                                break;
                            case 3:
                                if (flag == 1) {//增加、修改、删除分别为：0、1、2
                                    if (person.getPush_img_path3() != null) {
                                        photoFile = new File(person.getPush_img_path3());
                                        whiteFea = person.getPush_fea_path3();
                                    }
                                }
                                person.setPush_fea_path3(whiteFea);
                                person.setPush_img_path3(path);
                                break;
                            default:
                                if (flag == 1) {//增加、修改、删除分别为：0、1、2
                                    if (person.getImg_path() != null) {
                                        photoFile = new File(person.getImg_path());
                                        whiteFea = person.getFea_path();
                                    }
                                }
                                person.setFea_path(whiteFea);
                                person.setImg_path(path);
                                break;
                        }
                        if (photoFile != null && photoFile.exists()) {
                            Logger.d("下发人员  图片 删除 photoFile = " + photoFile.getAbsolutePath()
                                    + ",删除状态：" + photoFile.delete());
                        }
                        FeaUtils.saveFea(whiteFea, bitFea);
                        SubstationApplication.getApplication().getDaoSession().getPersonDao().insertOrReplace(person);
                        Logger.d("下发    人员图片，添加成功");
                        List<Person> changList = new ArrayList<>();
                        changList.add(person);
                        SubstationApplication.getApplication().getDaoSession().getPersonDao().insertOrReplaceInTx(changList);
                        handler.post(() -> Toast.makeText(WhiteListService.this,
                                R.string.person_save_success, Toast.LENGTH_SHORT).show());
                        modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_PHOTO_UPDATE, operateTime, true);
                    } else {
                        file.delete();
                        handler.post(() -> {
//                            Toast.makeText(WhiteListService.this, " 添加ID：" + person.getIc_card() + " 失败。" +
//                                    "\n失败原因：无法提取模版图片特征值，请确认模版图片有效", Toast.LENGTH_LONG).show();
                            handler.post(() -> Toast.makeText(WhiteListService.this,
                                    R.string.person_save_fail, Toast.LENGTH_SHORT).show());
                            Logger.e("下发人员图片，添加失败");
                        });
                        modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_PHOTO_UPDATE, operateTime, false);
                    }
                    modifyPersonPhotoList.remove(0);
                }

            } catch (IOException | JSONException | NullPointerException e) {
                Logger.e("下发人员图片，添加异常");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                isPersonPhotoSaving = false;
                MainModel.runDetect = false;//long true
            }
        });
    }

    private void cleanPerson(String operateTime) {
        ThreadPoolUtils.execute(() -> {
            try {
                List<Person> list = SubstationApplication.getApplication().getDaoSession()
                        .getPersonDao().loadAll();
                for (Person person : list) {
                    FileUtils.deleteFile(person.getAllFilePath());
                }
                Logger.d("下发删除全部   人员成功");
                SubstationApplication.getApplication().getDaoSession().getPersonDao().deleteInTx(list);
                modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_REMOVE, operateTime, true);
            } catch (Exception e) {
                e.printStackTrace();
                modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_REMOVE, operateTime, false);
            }
        });
    }

    private void removePerson(final String uid, String operateTime) {
        ThreadPoolUtils.execute(() -> {
            try {
                QueryBuilder<Person> qb = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder()
                        .where((PersonDao.Properties.Uid).eq(uid));
                List<Person> list = qb.list();
                Logger.d("list.size()=" + list.size());
                if (list.size() != 0) {
                    SubstationApplication.getApplication().getDaoSession().getPersonDao().deleteInTx(list);
                }
                for (Person person : list) {
                    FileUtils.deleteFile(person.getAllFilePath());
                }
                modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_REMOVE, operateTime, true);
            } catch (Exception e) {
                e.printStackTrace();
                modifyPersonMsgBack(NET_DOWNLOAD_ROSTER_REMOVE, operateTime, false);
            }
        });
    }

    /**
     * 对人员操作后，需要反馈
     */
    private void modifyPersonMsgBack(int msgType, String operateTime, boolean isSuccessful) {
        List<String> msgKeyList = new ArrayList<>();
        List<Object> msgValueList = new ArrayList<>();
        msgKeyList.add("Serial");//设备序列号
        msgKeyList.add("OperateTime");//操作时间(格式"yyyy-MM-dd HH:mm:ss.fff")
        msgKeyList.add("OperateType");//操作类型（名单信息更新、名单照片更新、名单删除、名单清空）
        msgKeyList.add("IsSucceed");//是否成功
        msgValueList.add(sn);
        msgValueList.add(operateTime);
        msgValueList.add(msgType);
        msgValueList.add(isSuccessful);
        Logger.i("modifyPersonMsgBack      msgType= " + msgType);
        sendMsg2(msgKeyList, msgValueList, NET_DOWNLOAD_ROSTER_OPERATE_BACK);
    }

    private void uploadHistory(final History history) {
        if (socket != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Uid", history.getIc_card());
                jsonObject.put("CIc", history.getIc_card());
                jsonObject.put("CompareType", history.getCompareType());
                jsonObject.put("Serial", sn);
                jsonObject.put("CreateTime", history.getTime());
                jsonObject.put("DScore", history.getScore());
                jsonObject.put("DRes", history.getResult());

                Bitmap templateBitmap = BitmapFactory.decodeFile(history.getTemplatePhotoPath());
                String templatePhoto = bitmapToBase64(templateBitmap);
                jsonObject.put("TemplateImg", templatePhoto);

                Bitmap siteBitmap = BitmapFactory.decodeFile(history.getFace_path());
                String sidePhoto = bitmapToBase64(siteBitmap);
                jsonObject.put("SiteImg", sidePhoto);
                jsonObject.put("InOut", history.getInOut());
                jsonObject.put("UpdateState", 1);
                baseSendMsg(jsonObject, NET_UPLOAD_RECORD);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void reuploadHistory() {
        if (socket != null) {
            if (reuploadThread == null || (reuploadThread.isDone() || reuploadThread.isCancelled())) {
                reuploadThread = ThreadPoolUtils.submit(() -> {
                    List<History> reuploadHistoryList = SubstationApplication.getApplication().getDaoSession().getHistoryDao().queryBuilder()
                            .where(HistoryDao.Properties.Upload_status.eq(false)).list();
                    for (History history : reuploadHistoryList) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("Uid", history.getIc_card());
                            jsonObject.put("CIc", history.getIc_card());
                            jsonObject.put("CompareType", history.getCompareType());
                            jsonObject.put("Serial", sn);
                            jsonObject.put("CreateTime", history.getTime());
                            jsonObject.put("DScore", history.getScore());
                            jsonObject.put("DRes", history.getResult());

                            Bitmap templateBitmap = BitmapFactory.decodeFile(history.getTemplatePhotoPath());
                            String templatePhoto = bitmapToBase64(templateBitmap);
                            jsonObject.put("TemplateImg", templatePhoto);

                            Bitmap siteBitmap = BitmapFactory.decodeFile(history.getFace_path());
                            String sidePhoto = bitmapToBase64(siteBitmap);
                            jsonObject.put("SiteImg", sidePhoto);
                            jsonObject.put("InOut", history.getInOut());
                            jsonObject.put("UpdateState", 2);//1实时数据，2历史数据
                            Logger.i("reuploadHistory    CIc = " + history.getIc_card() + ",CreateTime = " + history.getTime());
                            baseSendMsg(jsonObject, NET_UPLOAD_RECORD);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void uploadSuccess(String uid, String createTime) {
        //2018-01-20 16:13:43
        try {
            List<MsgUseElectricRecord> msgUseElectricRecordList = SubstationApplication.getApplication().getDaoSession().getMsgUseElectricRecordDao().queryBuilder()
                    .where(MsgUseElectricRecordDao.Properties.Create_time.eq(createTime), MsgUseElectricRecordDao.Properties.Uid.eq(uid)).list();
            if (msgUseElectricRecordList.size() == 1) {
                msgUseElectricRecordList.get(0).setUpload_status(true);
                Logger.e("用电记录上传成功！》》》》");
                SubstationApplication.getApplication().getDaoSession().getMsgUseElectricRecordDao().update(msgUseElectricRecordList.get(0));
                Logger.d("上传历史记录成功，设置记录上传状态为 true   比对记录的 比对时间为 = " + msgUseElectricRecordList.get(0).getCreate_time());
                List<Person> persons = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder()
                        .where(PersonDao.Properties.Uid.eq(msgUseElectricRecordList.get(0).getUid())).list();
                if (persons.size() != 0) {
                    for (Person p : persons) {
                        if (p.getPerson_type() == 2) {
                            FileUtils.deleteFile(p.getImg_path());
                        }
                    }
                }
            } else if (msgUseElectricRecordList.size() == 0) {
                Logger.e("上传历史记录成功，查询uid发现       0     个结果");
            } else {
                Logger.e("上传历史记录成功，查询uid发现      多     个结果");
                msgUseElectricRecordList.get(0).setUpload_status(true);
                SubstationApplication.getApplication().getDaoSession().getMsgUseElectricRecordDao().update(msgUseElectricRecordList.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static OnCardDetectListener onCardDetectListener;

    public static void setOnCardDetectListener(OnCardDetectListener listener) {
        onCardDetectListener = listener;
    }

//    public static Person personCache;

    private class NfcReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_NFC_RECEIVER_CARD_NUM.equals(action)) {
                byte[] extraId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Logger.d("NfcReceiver  extraId = " + Arrays.toString(extraId));//[-77, -36, -43, -7]
//                final String id = FileUtils.bytes2HexString(extraId);
//                assert id != null;
//                handleCardNum(id.toUpperCase());
                StringBuilder id = new StringBuilder();
                for (byte b : extraId) {
                    String hex = Integer.toHexString((int) b & 0xff);
                    if (hex.length() == 1) {
                        hex = '0' + hex;
                    }
                    id.append(hex);
                    Logger.d("NfcReceiver  id = " + id.toString());//[-77, -36, -43, -7]
                }
                handleCardNum(id.toString().toUpperCase());//9F11A2C
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {//判断其中一个就可以了
                Logger.d("USB已经连接！");
                ThreadPoolUtils.execute(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String path = XmlUtil.getPath();
                    if (path != null) {
                        List<String> config = XmlUtil.readXml(path);
                        if (config.size() != 0) {
                            if (!config.get(1).equals(Preference.getServerIp())
                                    || !config.get(2).equals(Preference.getServerPort())) {
                                Preference.setCustomName(config.get(0));
                                Preference.setServerIp(config.get(1));
                                Preference.setServerPort(config.get(2));
                                OnSettingChange(config.get(1));
                                Logger.d("U盘导入，ip或端口改变");
                            } else if (!config.get(0).equals(Preference.getCustomName())
                                    || !config.get(3).equals(Preference.getDoorway())) {
//                                Preference.setDoorway(config.get(3));
                                Preference.setCustomName(config.get(0));
                                OnSettingChange("name");
                                Logger.d("U盘导入，ip端口不变，改了配置");
                            }

//                            Preference.setCustomName(config.get(0));
//                            Preference.setServerIp(config.get(1));
//                            Preference.setServerPort(config.get(2));
//                            Logger.e("配置的ip地址为："+config.get(1));
//                            Logger.e("配置的端口为："+config.get(2));
//                            OnSettingChange();
                            handler.post(() ->
                                    Toast.makeText(SubstationApplication.getApplication(), "配置文件讀取成功", Toast.LENGTH_LONG).show()
                            );
                            Logger.e("配置文件讀取成功");
                        } else {
                            handler.post(() ->
                                    Toast.makeText(SubstationApplication.getApplication(), "U盘讀取配置文件異常", Toast.LENGTH_LONG).show()
                            );
                            Logger.e("U盘读取配置文件异常");
//                            XmlUtil.saveXml(path, "测试", "192.168.2.133", 1);
                        }
                    }
                });
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {//USB被拔出
                Logger.d("USB连接断开！");

            }
        }
    }

    private void handleCardNum(String id) {
        Logger.d("id=" + id);
        QueryBuilder<Person> builder = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder()
                .where(PersonDao.Properties.Uid.eq(id));
        final List<Person> personList = builder.list();
        Logger.d("personList.size = " + personList);
        handler.post(() -> {
            switch (personList.size()) {
                case 0:
                    Toast.makeText(SubstationApplication.getApplication(), R.string.person_no_import, Toast.LENGTH_LONG).show();
                    return;
                case 1:
//                    personCache = personList.get(0);
                    if (!Constant.isDoubleScreen) {
                        Intent i = new Intent(WhiteListService.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        WhiteListService.this.startActivity(i);
                    }
                    if (onCardDetectListener != null) {
                        onCardDetectListener.onDetectCard(personList.get(0));
                    }
                    break;
                default:
                    Toast.makeText(SubstationApplication.getApplication(), R.string.multiple_person, Toast.LENGTH_LONG).show();
            }
        });
    }

    //--------------------------------------------------------------------------------------

    private SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private boolean isTestRun;

    /**
     * 打開串口：用于与物理键盘进行通讯
     */
    private void OpenSerial() {
        // 打开
        try {
            String prot = "ttyS3";
            int baudrate = 9600;
            mSerialPort = new SerialPort(new File("/dev/" + prot), baudrate, 0);
            Logger.d("  串口打开        " + mSerialPort.toString());
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();

            //接收
            serialPortStartRead();

        } catch (SecurityException | IOException e) {
            Logger.d("  串口打开失败");
            e.printStackTrace();
            try {
                Thread.sleep(10000);
                OpenSerial();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    //    private List<Byte> messageList = new ArrayList<>();
    // 接收
    private void serialPortStartRead() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                isTestRun = true;
                while (isTestRun) {
                    int size;
                    try {
                        byte[] buffer = new byte[1];
                        if (mInputStream == null) {
                            Thread.sleep(10000);
                            OpenSerial();
                            return;
                        } else {
                            mInputStream = mSerialPort.getInputStream();
                        }
                        size = mInputStream.read(buffer);
                        if (size > 0) {
                            int typeInput = buffer[0];
                            handlerInputContent(typeInput);//输入的接收
//                            messageList.add(buffer[0]);

//                            Logger.d("串口 接收  信息 =" + Arrays.toString(messageList.toArray()));//串口 接收  信息 =[-2, 5, 0, 0, 0, 0, -39, -59]

//                            messageList.clear();

//                    if (messageList.size() == 4) {
////                        Logger.d("4 byte 卡号 =" + Arrays.toString(messageList.toArray()));//[-97, 17, -94, 12]
//                        StringBuilder id = new StringBuilder();
//                        for (byte b : messageList) {
//                            id.append(Integer.toHexString((int) b & 0xff));
//                        }
//                        handleCardNum(id.toString().toUpperCase());//9F11A2C
//                        messageList.clear();
//                    }
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //關閉串口
    private void closeSerialPort() {
        isTestRun = false;
        if (mSerialPort != null) {
            try {
                Logger.d("close serial port");
                mSerialPort.close();
                mSerialPort = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        messageList.clear();
    }

    private void handlerInputContent(int typeInput) {
        //过滤一遍
        String inputContent = "";
        switch (typeInput) {
            case 8://清除
                inputContent = "d";
                break;
            case 13://确认
                inputContent = "#";
                break;
            case 27:
                inputContent = "";
                break;
            case 33:
                inputContent = "";
                break;
            case 34:
                inputContent = "";
                break;
            case 46://.
                inputContent = "";
                break;
            case 48:
                inputContent = "0";
                break;
            case 49:
                inputContent = "1";
                break;
            case 50:
                inputContent = "2";
                break;
            case 51:
                inputContent = "3";
                break;
            case 52:
                inputContent = "4";
                break;
            case 53:
                inputContent = "5";
                break;
            case 54:
                inputContent = "6";
                break;
            case 55:
                inputContent = "7";
                break;
            case 56:
                inputContent = "8";
                break;
            case 57:
                inputContent = "";
                break;
            default:
                inputContent = "";
                break;
        }
        sendBroadcastMsg(0, null, inputContent);
    }

    //===========================================================================================

    private SerialPort mAnotherSerialPort;
    protected InputStream mAnotherInputStream;
    protected OutputStream mAnotherOutputStream;
    private boolean isAnotherTestRun;

    /**
     * 打开另一个串口：用于与控制器通讯
     */
    private void OpenAnotherSerial() {
        // 打开
        try {
            String prot = "ttyS2";
            int baudrate = 9600;
            mAnotherSerialPort = new SerialPort(new File("/dev/" + prot), baudrate, 0);
            Logger.d("  另一个串口已打开        " + mAnotherSerialPort.toString());
            mAnotherInputStream = mAnotherSerialPort.getInputStream();
            mAnotherOutputStream = mAnotherSerialPort.getOutputStream();


            //接收
//            anotherSerialPortStartRead();

            //初始化另一个串口，也就是全关。
            initAnotherSerialPort();

        } catch (SecurityException | IOException e) {
            Logger.d("  另一个串口打开失败");
            e.printStackTrace();
            try {
                Thread.sleep(10000);
                OpenAnotherSerial();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void initAnotherSerialPort() {
        ThreadPoolUtils.execute(() -> {
            try {
                if (mAnotherSerialPort == null) {
                    OpenAnotherSerial();
                }
                byte[] bytes = hex2byte(CONTROL_CLOSE_ALL);//CONTROL_CLOSE_ALL
//                Logger.e("打开第一路电：成功");
                if (ObjectUtils.isNotEmpty(bytes)) {
                    mAnotherOutputStream.write(bytes);
                }
            } catch (Exception e) {
                Logger.i("发送失败");
                e.printStackTrace();
            }
        });
    }

    private String orderString;
    private int which;
    private int status;//用电状态，0：关电；1：取电

    //另一个串口的发送
    public void sendAnotherSerialPortMsg(long id, String inputContent, Person person, float score, String face_path, Bitmap faceBit, String date) {
        ThreadPoolUtils.execute(() -> {
            try {
                if (mAnotherSerialPort == null) {
                    OpenAnotherSerial();
                }

                switch (inputContent) {
                    case "01":
                        which = 1;
                        if (!isOpen_1) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_1;
                            isOpen_1 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_1);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_1", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_1;
                            isOpen_1 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_1);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_1", null);//本不該加
                        }
                        break;
                    case "02":
                        which = 2;
                        if (!isOpen_2) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_2;
                            isOpen_2 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_2);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_2", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_2;
                            isOpen_2 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_2);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_2", null);//本不該加
                        }
                        break;
                    case "03":
                        which = 3;
                        if (!isOpen_3) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_3;
                            isOpen_3 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_3);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_3", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_3;
                            isOpen_3 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_3);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_3", null);//本不該加
                        }
                        break;
                    case "04":
                        which = 4;
                        if (!isOpen_4) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_4;
                            isOpen_4 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_4);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_4", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_4;
                            isOpen_4 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_4);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_4", null);//本不該加
                        }
                        break;
                    case "05":
                        which = 5;
                        if (!isOpen_5) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_5;
                            isOpen_5 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_5);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_5", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_5;
                            isOpen_5 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_5);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_5", null);//本不該加
                        }
                        break;
                    case "06":
                        which = 6;
                        if (!isOpen_6) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_6;
                            isOpen_6 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_6);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_6", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_6;
                            isOpen_6 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_6);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_6", null);//本不該加
                        }
                        break;
                    case "07":
                        which = 7;
                        if (!isOpen_7) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_7;
                            isOpen_7 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_7);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_7", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_7;
                            isOpen_7 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_7);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_7", null);//本不該加
                        }
                        break;
                    case "08":
                        which = 8;
                        if (!isOpen_8) {
                            //第一次進來就是開
                            orderString = CONTROL_OPEN_8;
                            isOpen_8 = true;
                            status = 1;
                            SPUtils.getInstance().put(inputContent, person.getId());//記錄誰開了哪路電
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_8);//記錄某路電是否開

                            sendBroadcastMsg(1, "open_8", null);//本不該加
                        } else {
                            //第二次進來就是關
                            orderString = CONTROL_CLOSE_8;
                            isOpen_8 = false;
                            status = 0;
                            SPUtils.getInstance().put(inputContent, 0);//清空記錄
                            SPUtils.getInstance().put("isOpen" + inputContent, isOpen_8);//記錄某路電是否開

                            sendBroadcastMsg(1, "close_8", null);//本不該加
                        }
                        break;
                    default:
                        break;
                }

                byte[] bytes = hex2byte(orderString);//CONTROL_OPEN_1   CONTROL_CLOSE_1
                if (ObjectUtils.isNotEmpty(bytes)) {
                    mAnotherOutputStream.write(bytes);
                }
                //上傳記錄
                //                uploadUseElectricRecord(id,person, score, face_path, which, status);
                saveAndUpload(id, person, score, face_path, which, status, faceBit, date);
            } catch (Exception e) {
                Logger.i("发送失败");
                e.printStackTrace();
            }
        });
    }

    //保存和上传用电记录数据
    private void saveAndUpload(long id, Person person, float score, String face_path, int which, int status, Bitmap faceBit, String date) {
        Bitmap templateBitmap = BitmapFactory.decodeFile(person.getImg_path());
        String templatePhoto = bitmapToBase64(templateBitmap);

        ImgUtils.getUtils().saveBitmap(faceBit, face_path);
        String sidePhoto = bitmapToBase64(faceBit);

        MsgUseElectricRecord msgUseElectricRecord = new MsgUseElectricRecord();
        msgUseElectricRecord.setId(id);
        msgUseElectricRecord.setUid(person.getUid());
        msgUseElectricRecord.setSerial_num(sn);
        msgUseElectricRecord.setCreate_time(date);
        msgUseElectricRecord.setScore_num(String.valueOf(score));
        msgUseElectricRecord.setDetectInfo(0);
        msgUseElectricRecord.setTemplate_image(templatePhoto);
        msgUseElectricRecord.setSite_image(sidePhoto);
        msgUseElectricRecord.setUpdateState(1);
        msgUseElectricRecord.setElectric_num(which);//某路电：1,2,3,4,5,6,7,8
        msgUseElectricRecord.setUseElectricState(status);//用电状态，0：关电；1：取电
        msgUseElectricRecord.setException_info(0);//异常信息，0：正常；1：漏电；2：断电；3：其它

        ThreadPoolUtils.execute(() -> {
            if (SubstationApplication.getApplication().getDaoSession().getMsgUseElectricRecordDao().count() >= 10000) {
                try {
                    if (Preference.getHisFirstIdId() != null && !Preference.getHisFirstIdId().equals("")) {
                        long firstID = Long.parseLong(Preference.getHisFirstIdId());

                        MsgUseElectricRecord record = SubstationApplication.getApplication().getDaoSession().getMsgUseElectricRecordDao().load(firstID);
                        FileUtils.deleteFile(record.getSite_image());
                        FileUtils.deleteFile(record.getTemplate_image());
                        SubstationApplication.getApplication().getDaoSession().delete(record);
                        firstID++;

                        Preference.setHisFirstId("" + firstID);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            Preference.setHisLastId("" + id);
            SubstationApplication.getApplication().getDaoSession().insert(msgUseElectricRecord);
            Logger.e("用电记录保存成功》》》》》");
            uploadUseElectricRecord(msgUseElectricRecord);
        });
    }

    //上传用电记录数据
    public void uploadUseElectricRecord(MsgUseElectricRecord msgUseElectricRecord) {
        //long id,Person person, float score, String face_path, int which, int status
        if (socket != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Uid", msgUseElectricRecord.getUid());
                jsonObject.put("Serial", sn);
                jsonObject.put("CreateTime", msgUseElectricRecord.getCreate_time());//當前時間
                jsonObject.put("DScore", msgUseElectricRecord.getScore_num());
                jsonObject.put("DRes", 0);

                jsonObject.put("TemplateImg", msgUseElectricRecord.getTemplate_image());//模板照
                jsonObject.put("SiteImg", msgUseElectricRecord.getSite_image());

                jsonObject.put("UpdateState", 1);
                jsonObject.put("ElectricNumber", msgUseElectricRecord.getElectric_num());//某路电：1,2,3,4,5,6,7,8
                jsonObject.put("UseElectricState", msgUseElectricRecord.getUseElectricState());//用电状态，0：关电；1：取电
                jsonObject.put("ExceptionInfo", 0);//异常信息，0：正常；1：漏电；2：断电；3：其它

                baseSendMsg(jsonObject, NET_UPLOAD_USEELECTRIC_RECORD);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //重新上传用电记录的历史数据
    private void reUploadUseElectricRecord() {
        if (socket != null) {
            if (reuploadThread == null || (reuploadThread.isDone() || reuploadThread.isCancelled())) {
                reuploadThread = ThreadPoolUtils.submit(() -> {
                    List<MsgUseElectricRecord> msgUseElectricRecordList = SubstationApplication.getApplication().getDaoSession().getMsgUseElectricRecordDao().queryBuilder()
                            .where(MsgUseElectricRecordDao.Properties.Upload_status.eq(false)).list();
                    for (MsgUseElectricRecord msgUseElectricRecord : msgUseElectricRecordList) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("Uid", msgUseElectricRecord.getUid());
                            jsonObject.put("Serial", sn);
                            jsonObject.put("CreateTime", msgUseElectricRecord.getCreate_time());//當前時間
                            jsonObject.put("DScore", msgUseElectricRecord.getScore_num());
                            jsonObject.put("DRes", 0);

                            jsonObject.put("TemplateImg", msgUseElectricRecord.getTemplate_image());//模板照
                            jsonObject.put("SiteImg", msgUseElectricRecord.getSite_image());

                            jsonObject.put("UpdateState", 2);
                            jsonObject.put("ElectricNumber", msgUseElectricRecord.getElectric_num());//某路电：1,2,3,4,5,6,7,8
                            jsonObject.put("UseElectricState", msgUseElectricRecord.getUseElectricState());//用电状态，0：关电；1：取电
                            jsonObject.put("ExceptionInfo", 0);//异常信息，0：正常；1：漏电；2：断电；3：其它

                            baseSendMsg(jsonObject, NET_UPLOAD_USEELECTRIC_RECORD);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private List<Byte> anotherMessageList = new ArrayList<>();
    private boolean isAdd = false;
    private int sleepTime;

    //另一个串口的接收
    private void anotherSerialPortStartRead() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                isAnotherTestRun = true;
                while (isAnotherTestRun) {
                    int size;
                    try {
                        byte[] buffer = new byte[1];
                        if (mAnotherInputStream == null) {
                            Thread.sleep(10000);
                            OpenAnotherSerial();
                            return;
                        } else {
                            mAnotherInputStream = mAnotherSerialPort.getInputStream();
                        }
                        size = mAnotherInputStream.read(buffer);

                        if (size > 0) {
                            int typeInput = buffer[0];
                            //第一個接收
                            StringBuilder stringBuilder = new StringBuilder();
                            String firstString = stringBuilder.append(Integer.toHexString((int) typeInput & 0xff)).toString().toUpperCase();
//                            Logger.e("第一個是：" + firstString);

                            if (firstString.equals("FE")) {
                                isAdd = true;

                                sleepTime = 100;
//                                if (anotherMessageList.size()>0){
                                //處理命令
                                StringBuilder callbackMsg = new StringBuilder();
                                for (byte b : anotherMessageList) {
                                    callbackMsg.append(Integer.toHexString((int) b & 0xff));
                                }
                                Logger.d("配电箱指令返回的十六进制信息：" + callbackMsg.toString().toUpperCase());
//                                    handlerOrderMsg(callbackMsg.toString().toUpperCase());
                                anotherMessageList.clear();
//                                    sleepTime = 100;
//                                }

//                                sleepTime = 100;
//                                //開啟線程
//                                ThreadPoolUtils.execute(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            Thread.sleep(sleepTime);
//
//                                            if (anotherMessageList.size()>0){
//                                            //處理命令
//                                            StringBuilder callbackMsg = new StringBuilder();
//                                            for (byte b : anotherMessageList) {
//                                                callbackMsg.append(Integer.toHexString((int) b & 0xff));
//                                            }
//                                            Logger.d("配电箱指令返回的十六进制信息：" + callbackMsg.toString().toUpperCase());
//
//                                            anotherMessageList.clear();
//                                            isAdd = false;
//                                            }
//
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });

                                //開啟線程


                            }

                            if (isAdd) {
                                anotherMessageList.add(buffer[0]);
                            }


                            Logger.d("另一個串口 接收  信息 =" + Arrays.toString(anotherMessageList.toArray()));

                            //输出指令的返回信息
//                            if (anotherMessageList.size() == 8) {
//                                StringBuilder callbackMsg = new StringBuilder();
//                                for (byte b : anotherMessageList) {
//                                    callbackMsg.append(Integer.toHexString((int) b & 0xff));
//                                }
//                                Logger.d("配电箱指令返回的十六进制信息：" + callbackMsg.toString().toUpperCase());
//                                handlerOrderMsg(callbackMsg.toString().toUpperCase());
//                                anotherMessageList.clear();
//                            }

                        }


                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //关闭另一个串口
    private void closeAnotherSerialPort() {
        isAnotherTestRun = false;
        if (mAnotherSerialPort != null) {
            try {
                Logger.d("close another serial port");
                mAnotherSerialPort.close();
                mAnotherSerialPort = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mAnotherInputStream != null) {
            try {
                mAnotherInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mAnotherOutputStream != null) {
            try {
                mAnotherOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        anotherMessageList.clear();
    }

    /**
     * @param orderMsg 处理不同指令信息
     */
    private void handlerOrderMsg(String orderMsg) {
        //开：FE500FF09835   FE501FF0C9F5   FE502FF039F5   FE503FF06835   FE504FF0D9F4   FE505FF08834   FE506FF07834   FE507FF029F4   FEF0008402（全开）     FE110619CFE2（状态）
        //关：FE50000D9C5    FE50100885     FE50200785     FE5030029C5    FE50400984     FE50500C9C4    FE5060039C4    FE50700684     FEF0008402（全关）
        if (ObjectUtils.isNotEmpty(orderMsg)) {
            switch (orderMsg) {
//                case "FEFE500FF098":FEFEFE500FF0
                case "FE500FF09835":
                    sendBroadcastMsg(1, "open_1", null);
                    break;
                case "FE50000D9C5":
                    sendBroadcastMsg(1, "close_1", null);
                    break;
                case "FE501FF0C9F5":
                    sendBroadcastMsg(1, "open_2", null);
                    break;
                case "FE50100885":
                    sendBroadcastMsg(1, "close_2", null);
                    break;
                case "FE502FF039F5":
                    sendBroadcastMsg(1, "open_3", null);
                    break;
                case "FE50200785":
                    sendBroadcastMsg(1, "close_3", null);
                    break;
                case "FE503FF06835":
                    sendBroadcastMsg(1, "open_4", null);
                    break;
                case "FE5030029C5":
                    sendBroadcastMsg(1, "close_4", null);
                    break;
                case "FE504FF0D9F4":
                    sendBroadcastMsg(1, "open_5", null);
                    break;
                case "FE50400984":
                    sendBroadcastMsg(1, "close_5", null);
                    break;
                case "FE505FF08834":
                    sendBroadcastMsg(1, "open_6", null);
                    break;
                case "FE50500C9C4":
                    sendBroadcastMsg(1, "close_6", null);
                    break;
                case "FE506FF07834":
                    sendBroadcastMsg(1, "open_7", null);
                    break;
                case "FE5060039C4":
                    sendBroadcastMsg(1, "close_7", null);
                    break;
                case "FE507FF029F4":
                    sendBroadcastMsg(1, "open_8", null);
                    break;
                case "FE50700684":
                    sendBroadcastMsg(1, "close_8", null);
                    break;
                case "FEF0008402":
//                    ToastUtils.showShort("已经初始化控制器");
                    Logger.e("控制器已初始化");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @param broadcastType 0：一个串口；1：另一个串口
     */
    private void sendBroadcastMsg(int broadcastType, String openOrClose, String inputContent) {
        Intent intent = new Intent();
        intent.putExtra("broadcastType", broadcastType);
        if (broadcastType == 0) {
            intent.putExtra("inputContent", inputContent);
        } else if (broadcastType == 1) {
            //另一个串口的接收
            intent.putExtra("openOrClose", openOrClose);
        }
        intent.setAction("com.taisau.substation.WorkmanInputReceiver");
        sendBroadcast(intent);
    }

    //----------------------------工具方法（固定）---------------------------------------------------

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和 @Code{intToBytes()}配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    public byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * base64ToBitmap
     */
    public static Bitmap base64ToBitmap(String base64String) {
        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }

    /**
     * bitmap转为base64
     */
    public String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    // 判断字节数组前几位是否符合一定规则
    public static boolean isHeadMatch(byte[] data, byte[] pattern) {
        if (data == null || data.length < pattern.length)
            return false;
        for (int i = 0; i < pattern.length; i++) {
            if (data[i] != pattern[i])
                return false;
        }
        return true;
    }

    // 判断字节数组后几位是否符合一定规则
    public static boolean isTailMatch(byte[] data, byte[] pattern) {
        if (data == null || data.length < pattern.length)
            return false;
        for (int i = 0; i < pattern.length; i++) {
            if (data[data.length - pattern.length + i] != pattern[i])
                return false;
        }
        return true;
    }

    //十六进制转byte
    private byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }
}
