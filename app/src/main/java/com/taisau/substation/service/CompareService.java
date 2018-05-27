package com.taisau.substation.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.serialport.api.SerialPort;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.SubstationApplication;
import com.taisau.substation.bean.Person;
import com.taisau.substation.bean.PersonDao;
import com.taisau.substation.listener.OnCardDetectListener;
import com.taisau.substation.listener.OnConnectStatusChangeListener;
import com.taisau.substation.util.Preference;
import com.taisau.substation.util.ThreadPoolUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CompareService extends Service {

    private final CompareBinder binder = new CompareBinder();
    private Socket socket;
    private boolean isRun;
    private volatile boolean hasPermission;
    private Handler handler = new Handler();

    private SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;

    private List<UsbDevice> connectedDevices = new ArrayList<>();
    private Thread getDeviceThread;

    public CompareService() {
    }

    public class CompareBinder extends Binder {
        public CompareService getService() {
            return CompareService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        ThreadPoolUtils.execute(this::OpenSerial);
//        initUsbHost();//主线程初始化
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void openTcp() {
        try {
            String ip = Preference.getGateIp();
            if (ip == null || ip.equals("")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CompareService.this, R.string.no_ip, Toast.LENGTH_LONG).show();
                    }
                });
                Thread.sleep(30000);
                openTcp();
            } else {
                String portStr = Preference.getGatePort();
                int port;
                if (portStr == null || portStr.equals("")) {
                    port = 9010;
                } else {
                    port = Integer.valueOf(portStr);
                }
                socket = new Socket(ip, port);
                if (!socket.isConnected()) {
                    Logger.d(" 闸机    socket   no connect");
                    Thread.sleep(10000);
                    openTcp();
                } else {
                    tcpStartRead();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            ThreadPoolUtils.execute(this::openTcp);
        }
    }

    private void tcpStartRead() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                isRun = true;
                while (isRun) {
                    if (socket == null || !socket.isConnected()) {
                        isRun = false;
                        openTcp();
                        return;
                    }
                    InputStream mInStream = null;
                    try {
                        mInStream = socket.getInputStream();

                        final byte[] buffer = new byte[4];
                        final int count = mInStream.read(buffer);
                        if (count > 0) {
                            //TODO 读取 IC 卡的 id 回调给 MainModel 去比对；
                   /* final String id = new String(buffer, "UTF-8");
                    Logger.d("count="+count+",收到闸机下发的id = " + id);
                    */
                            StringBuilder str = new StringBuilder(Integer.toHexString((int) buffer[0] & 0xff))
                                    .append(Integer.toHexString((int) buffer[1] & 0xff))
                                    .append(Integer.toHexString((int) buffer[2] & 0xff))
                                    .append(Integer.toHexString((int) buffer[3] & 0xff));
//                    final int icKey=((int)buffer[0]<<24)+((int)buffer[1]<<16)+((int)buffer[2]<<8)+((int)buffer[3]);
//                    final int id = icKey;
                            final String id = str.toString();
                            Logger.d("str=" + str);
                            QueryBuilder<Person> builder = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder()
                                    .where(PersonDao.Properties.Ic_card.eq(id));
                            final List<Person> personList = builder.list();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    switch (personList.size()) {
                                        case 0:
                                            Toast.makeText(SubstationApplication.getApplication(), R.string.person_no_import, Toast.LENGTH_LONG).show();
                                            return;
                                        case 1:
                                            onCardDetectListener.onDetectCard(personList.get(0));
                                            break;
                                        default:
                                            Toast.makeText(SubstationApplication.getApplication(), R.string.multiple_person, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e.getMessage().contains("java.net.SocketException: Socket closed")) {
                            ThreadPoolUtils.execute(new Runnable() {
                                @Override
                                public void run() {
                                    openTcp();
                                }
                            });
                            return;
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void closeTcp() {
        isRun = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTcpMsg(final String msg) {
        if (socket != null) {
            try {
                socket.getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static OnCardDetectListener onCardDetectListener;

    public static void setOnCardDetectListener(OnCardDetectListener listener) {
        onCardDetectListener = listener;
    }

    private void OpenSerial() {
        // 打开
        try {
            String prot = "ttyS3";
            int baudrate = 9600;
            mSerialPort = new SerialPort(new File("/dev/" + prot), baudrate, 0);
            Logger.d("  串口打开        " + mSerialPort.toString());
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
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

    private List<Byte> messageList = new ArrayList<>();

    private void serialPortStartRead() {
        // 接收
        isRun = true;
        while (isRun) {
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
//                if (size > 0) {
//                    messageList.add(buffer[0]);
//                    Logger.d("串口 接收  信息 =" + Arrays.toString(messageList.toArray()));
//                    if (messageList.size() == 4) {
//                        Logger.d("4 byte 卡号 =" + Arrays.toString(messageList.toArray()));
//                        StringBuilder id = new StringBuilder();
//                        for (byte b : messageList) {
//                            id.append(Integer.toHexString((int) b & 0xff));
//                        }
//                        handleCardNum(id.toString().toUpperCase());
//                        messageList.clear();
//                    }
//                }
                StringBuilder sb = new StringBuilder();
                if ((size > 0) && validCardNum(buffer[0])) {
                    char c = (char) buffer[0];
                    sb.append(c);

                } else if (buffer[0] == 0x03) {
                    String num = bytesToAscii(sb.toString().substring(0, sb.length() - 2).getBytes());// TODO: 2018-03-20 验证卡号是否正确
                    sb.setLength(0);
                    handleCardNum(num.toUpperCase());
                            /*
                            发送数据格式（HEX）： 02 （十个字节卡片数据） 0D 0A 03，十字节卡
                            片数据中前 8 个字节是卡号，末 2 个字节是校验字节（ASCII 码方式）

                             E/___WW___: buf= 2   //start
                             E/___WW___: buf= 48  0
                             E/___WW___: buf= 48  0
                             E/___WW___: buf= 52  4
                             E/___WW___: buf= 55  7
                             E/___WW___: buf= 55  7
                             E/___WW___: buf= 55  7
                             E/___WW___: buf= 53  5
                             E/___WW___: buf= 48  0
                             E/___WW___: buf= 54  6
                             E/___WW___: buf= 48  0
                             E/___WW___: buf= 13  \n
                             E/___WW___: buf= 10  \r
                             E/___WW___: buf= 3   //end
                             */
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validCardNum(byte bNum) {
        if ((bNum >= 0x30 && bNum <= 0x39) || (bNum >= 0x41 && bNum <= 0x5a) || (bNum >= 0x61 && bNum <= 0x7a)) {
            return true;
        }
        return false;
    }

    public static String bytesToAscii(byte[] bytes, int offset, int dateLen) {
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dateLen <= 0)) {
            return null;
        }
        if ((offset >= bytes.length) || (bytes.length - offset < dateLen)) {
            return null;
        }

        String asciiStr = null;
        byte[] data = new byte[dateLen];
        System.arraycopy(bytes, offset, data, 0, dateLen);
        try {
            asciiStr = new String(data, "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
        }
        return asciiStr;
    }

    public static String bytesToAscii(byte[] bytes, int dateLen) {
        return bytesToAscii(bytes, 0, dateLen);
    }

    public static String bytesToAscii(byte[] bytes) {
        return bytesToAscii(bytes, 0, bytes.length);
    }

    public void sendSerialPortMsg() {
        // 发送
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSerialPort == null) {
                        OpenSerial();
                    }
                    //f0全开
                    //00全关
                    mOutputStream.write((byte) 0xf0);
                    Logger.i("发送成功:0xf0");
                    //开一秒，立马发送关闭指令，有人在闸机中间的话，闸机有红外感应，不会夹到人
                    Thread.sleep(1000);
                    mOutputStream.write((byte) 0x00);
                    Logger.i("发送成功:0x00 ");
                } catch (Exception e) {
                    Logger.i("发送失败");
                    e.printStackTrace();
                }
            }
        });
    }

    private void closeSerialPort() {
        isRun = false;
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
        messageList.clear();
    }

    @Override
    public void onDestroy() {
        closeTcp();
        closeSerialPort();
        closeUsbHost();
    }

    private void closeUsbHost() {
        OnConnectStatusChangeListener.OnUsbChange(false);
        hasPermission = false;
        if (getDeviceThread != null && getDeviceThread.isAlive()) {
            getDeviceThread.interrupt();
        }
        if (mUsbPermissionReciever != null) {
            unregisterReceiver(mUsbPermissionReciever);
            mUsbPermissionReciever = null;
        }
    }

    private UsbManager mUsbManager;
    private UsbReceiver mUsbPermissionReciever;
    PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private void initUsbHost() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Register for permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mUsbPermissionReciever = new UsbReceiver();
        registerReceiver(mUsbPermissionReciever, filter);

        Logger.d("初始化调用  getUsbDevice");
        getUsbDevice();
    }

    private void getUsbDevice() {

        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();

        if (deviceList == null || deviceList.size() == 0) {
            Logger.d("deviceList == null || deviceList.size() == 0");
            connectedDevices.clear();
            return;
        }
        // Request permission from user
        if (mPermissionIntent != null) {
            Set<String> key = deviceList.keySet();
            for (final String keyStr : key) {
                isStorageDevice(deviceList.get(keyStr));
                if (!connectedDevices.contains(deviceList.get(keyStr))) {
                    connectedDevices.add(deviceList.get(keyStr));
                } else {
                    Logger.d(deviceList.get(keyStr).getDeviceName() + "已添加过，不再处理");
                    continue;
                }
                if (deviceList.get(keyStr).getProductName() != null && deviceList.get(keyStr).getProductName().contains("Android")) {
//                if (deviceList.get(keyStr).getProductId() != 9488 && deviceList.get(keyStr).getProductId() != 774) {
//                if (deviceList.get(keyStr).getProductId() == 0x2d00 || deviceList.get(keyStr).getProductId() == 0x2d01) {
                    getDeviceThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (mUsbManager.hasPermission(deviceList.get(keyStr))) {
                                hasPermission = true;
                                Logger.d("判断有权限，直接调用initAccessory");
                                initAccessory(deviceList.get(keyStr));
                            } else {
                                mUsbManager.requestPermission(deviceList.get(keyStr), mPermissionIntent);
                               /* while (!mUsbManager.hasPermission(deviceList.get(keyStr))) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                hasPermission = true;
                                Logger.d("请求权限成功，调用initAccessory");
                                initAccessory(deviceList.get(keyStr));*/
                            }
                        }
                    });
                    getDeviceThread.start();

                }
            }
        } else {
            Logger.e("Device not present? Can't request peremission");
        }
//        if (searchForUsbAccessory(deviceList)) {
//            Logger.d("searchForUsbAccessory     true");
//            return;
//        }
//        for (UsbDevice device : deviceList.values()) {
//            initAccessory(device);
//        }
    }

    private boolean searchForUsbAccessory(final HashMap<String, UsbDevice> deviceList) {
        for (UsbDevice device : deviceList.values()) {
            if (isUsbAccessory(device)) {
                ThreadPoolUtils.execute(new CommunicationRunnable(device));
                return true;
            }
        }
        return false;
    }

    private boolean isUsbAccessory(final UsbDevice device) {
        return (device.getProductId() == 0x2d00) || (device.getProductId() == 0x2d01);
    }

    private boolean initAccessory(final UsbDevice device) {

        final UsbDeviceConnection connection = mUsbManager.openDevice(device);

        if (connection == null) {
            OnConnectStatusChangeListener.OnUsbChange(false);
            return false;
        }

//        initStringControlTransfer(connection, 0, "quandoo"); // MANUFACTURER
//        initStringControlTransfer(connection, 1, "Android2AndroidAccessory"); // MODEL
//        initStringControlTransfer(connection, 2, "showcasing android2android USB communication"); // DESCRIPTION
//        initStringControlTransfer(connection, 3, "0.1"); // VERSION
//        initStringControlTransfer(connection, 4, "http://quandoo.de"); // URI
//        initStringControlTransfer(connection, 5, "42"); // SERIAL
//        connection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, 100);
//        connection.close();

        //根据AOA协议打开Accessory模式
        initStringControlTransfer(connection, 0, "Google, Inc."); // MANUFACTURER
        initStringControlTransfer(connection, 1, "AccessoryChat"); // MODEL
        initStringControlTransfer(connection, 2, "Accessory Chat"); // DESCRIPTION
        initStringControlTransfer(connection, 3, "1.0"); // VERSION
        initStringControlTransfer(connection, 4, "http://www.android.com"); // URI
        initStringControlTransfer(connection, 5, "0123456789"); // SERIAL
        connection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, 100);
        connection.close();
        ThreadPoolUtils.execute(new CommunicationRunnable(device));
        return true;
    }

    private void initStringControlTransfer(final UsbDeviceConnection deviceConnection,
                                           final int index, final String string) {
        deviceConnection.controlTransfer(0x40, 52, 0, index, string.getBytes(), string.length(), 100);
    }

    private class UsbReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            //TODO openDevice
                            hasPermission = true;
                            ThreadPoolUtils.execute(new Runnable() {
                                @Override
                                public void run() {
                                    initAccessory(device);
                                }
                            });
                            Logger.d("ACTION_USB_PERMISSION  收到有权限的设备广播 device != null  ");
                        }
                    } else {
                        Logger.d(" ACTION_USB_PERMISSION  permission denied for device " + device);
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {//判断其中一个就可以了
                Logger.d("USB已经连接！ 调用 getUsbDevice");
                getUsbDevice();
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {//USB被拔出
                Logger.d("USB连接断开！");
//                closeUsbHost();
                final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                if (deviceList == null || deviceList.size() == 0) {
                    hasPermission = false;
                } else {
                    Set<String> key = deviceList.keySet();
                    connectedDevices.clear();
                    int i = 0;
                    for (String keyStr : key) {
                        connectedDevices.add(deviceList.get(keyStr));
                        Logger.d("deviceList.get(keyStr).getProductName()=" + deviceList.get(keyStr).getProductName());
                        if (deviceList.get(keyStr).getProductName() != null && deviceList.get(keyStr).getProductName().contains("Android")) {
                            Logger.d("getProductName()=" + deviceList.get(keyStr).getProductName());
                            hasPermission = true;
                        } else {
                            i++;
                        }
                    }
                    Logger.d("i = " + i + ",connectedDevices.size()=" + connectedDevices.size());
                    if (i == connectedDevices.size()) {
                        hasPermission = false;
                    }
                }
            }
        }
    }

    private boolean isStorageDevice(UsbDevice usbDevice) {
        int deviceClass = usbDevice.getDeviceClass();
        if (deviceClass == 0) {
            UsbInterface anInterface = usbDevice.getInterface(0);
            int interfaceClass = anInterface.getInterfaceClass();
            StringBuilder sb = new StringBuilder();
            sb.append("usbDevice=" + usbDevice.toString() + "\n");
            sb.append("device Class 为0-------------\n");
            sb.append("Interface.describeContents()=" + anInterface.describeContents() + "\n");
            sb.append("Interface.getEndpointCount()=" + anInterface.getEndpointCount() + "\n");
            sb.append("Interface.getId()=" + anInterface.getId() + "\n");

            //通过下面的InterfaceClass 来判断到底是哪一种的，例如7就是打印机，8就是usb的U盘
            sb.append("Interface.getInterfaceClass()=" + anInterface.getInterfaceClass() + "\n");
            if (anInterface.getInterfaceClass() == 7) {
                sb.append("此设备是打印机\n");
            } else if (anInterface.getInterfaceClass() == 8) {
                sb.append("此设备是U盘\n");
            }
            sb.append("anInterface.getInterfaceProtocol()=" + anInterface.getInterfaceProtocol() + "\n");
            sb.append("anInterface.getInterfaceSubclass()=" + anInterface.getInterfaceSubclass() + "\n");
            sb.append("device Class 为0------end-------\n");
            Logger.d(sb.toString());
            return anInterface.getInterfaceClass() == 8;
        } else {
            return false;
        }
    }

    private class CommunicationRunnable implements Runnable {
        private UsbDevice device = null;

        CommunicationRunnable(UsbDevice d) {
            this.device = d;
        }

        @Override
        public void run() {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbEndpoint endpointIn = null;
            UsbEndpoint endpointOut = null;

            final UsbInterface usbInterface = device.getInterface(0);

            for (int i = 0; i < device.getInterface(0).getEndpointCount(); i++) {

                final UsbEndpoint endpoint = device.getInterface(0).getEndpoint(i);
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    endpointIn = endpoint;
                }
                if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpointOut = endpoint;
                }

            }

            if (endpointIn == null) {
                Logger.d("Input Endpoint not found");
                return;
            }

            if (endpointOut == null) {
                Logger.d("Output Endpoint not found");
                return;
            }

            final UsbDeviceConnection connection = usbManager.openDevice(device);

            if (connection == null) {
                Logger.d("Could not open device");
                return;
            }

            final boolean claimResult = connection.claimInterface(usbInterface, true);

            if (!claimResult) {
                Logger.d("Could not claim device");
            } else {
                final byte buffer[] = new byte[4];
                Logger.d("Claimed interface - ready to communicate");
                if (hasPermission) {
                    OnConnectStatusChangeListener.OnUsbChange(true);
                }
                while (hasPermission) {
                    final int bytesTransferred = connection.bulkTransfer(endpointIn, buffer, buffer.length, 100);
                    if (bytesTransferred > 0) {
                        Logger.d("device> " + Arrays.toString(buffer));
                        StringBuilder id = new StringBuilder(Integer.toHexString((int) buffer[0] & 0xff))
                                .append(Integer.toHexString((int) buffer[1] & 0xff))
                                .append(Integer.toHexString((int) buffer[2] & 0xff))
                                .append(Integer.toHexString((int) buffer[3] & 0xff));
                        //TODO 回调给MainModel
                        handleCardNum(id.toString());
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                OnConnectStatusChangeListener.OnUsbChange(false);
                Logger.d("hasPermission==false   读取 usb 传输数据 结束 ");
            }

            connection.releaseInterface(usbInterface);
            connection.close();
        }
    }

    private void handleCardNum(String id) {
        Logger.d("id=" + id);
        QueryBuilder<Person> builder = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder()
                .where(PersonDao.Properties.Ic_card.eq(id));
        final List<Person> personList = builder.list();
        handler.post(() -> {
            switch (personList.size()) {
                case 0:
                    Toast.makeText(SubstationApplication.getApplication(), R.string.person_no_import, Toast.LENGTH_LONG).show();
                    return;
                case 1:
                    if (onCardDetectListener != null) {
                        onCardDetectListener.onDetectCard(personList.get(0));
                    }
                    break;
                default:
                    Toast.makeText(SubstationApplication.getApplication(), R.string.multiple_person, Toast.LENGTH_LONG).show();
            }
        });
    }
}
