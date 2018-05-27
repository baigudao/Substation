package com.taisau.substation;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import com.GFace;
import com.blankj.utilcode.util.Utils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.squareup.leakcanary.LeakCanary;
import com.taisau.substation.bean.DaoMaster;
import com.taisau.substation.bean.DaoSession;
import com.taisau.substation.service.WhiteListService;
import com.taisau.substation.util.CrashHandler;
import com.taisau.substation.util.FileUtils;
import com.taisau.substation.util.Preference;
import com.taisau.substation.util.ThreadPoolUtils;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileFilter;

import static com.taisau.substation.util.Constant.FACE_IMG;
import static com.taisau.substation.util.Constant.FILE_FACE6;
import static com.taisau.substation.util.Constant.FILE_FACE7;
import static com.taisau.substation.util.Constant.LIB_DIR;
import static com.taisau.substation.util.Constant.TEMPLATE_FEA;
import static com.taisau.substation.util.Constant.TEMPLATE_IMG;


/**
 * Created by whx on 2018/02/11
 */

public class SubstationApplication extends Application {

    private static SubstationApplication application;

    public static SubstationApplication getApplication() {
        return application;
    }

    private DaoSession daoSession;

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static WhiteListService whiteListService;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "faceSubstation");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        if (getDaoSession().getHistoryDao().count() == 0)
            Preference.setHisFirstId("1");

        File fileLibDir = new File(LIB_DIR);//storage/sdcard0/taisau_substation
        File fileFace6 = new File(FILE_FACE6);
        File fileFace7 = new File(FILE_FACE7);
        File templateImg = new File(TEMPLATE_IMG);
        File templateFea = new File(TEMPLATE_FEA);
        File faceImg = new File(FACE_IMG);
//        File faceFea = new File(LIB_DIR + "/face_fea");
//        File whiteImg = new File(LIB_DIR + "/white_img");
//        File whiteFea = new File(LIB_DIR + "/white_fea");
        if (!fileLibDir.exists())
            fileLibDir.mkdir();
        if (!fileFace6.exists())
            fileFace6.mkdir();
        if (!fileFace7.exists())
            fileFace7.mkdir();
        if (!templateImg.exists())
            templateImg.mkdir();
        if (!templateFea.exists())
            templateFea.mkdir();
        if (!faceImg.exists())
            faceImg.mkdir();
//        if (!faceFea.mkdir())
//            faceFea.mkdir();
//        if (!whiteImg.exists())
//            whiteImg.mkdir();
//        if (!whiteFea.exists())
//            whiteFea.mkdir();
        FileUtils.moveConfigFile(this, R.raw.base, LIB_DIR + "/base.dat");
        FileUtils.moveConfigFile(this, R.raw.license, LIB_DIR + "/license.lic");
        FileUtils.moveConfigFile(this, R.raw.anew, FILE_FACE6 + "/anew.dat");
        FileUtils.moveConfigFile(this, R.raw.dnew, FILE_FACE6 + "/dnew.dat");
        FileUtils.moveConfigFile(this, R.raw.db, FILE_FACE7 + "/db.dat");
        FileUtils.moveConfigFile(this, R.raw.p, FILE_FACE7 + "/p.dat");

        //崩溃日志
        if (!BuildConfig.DEBUG) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(getApplicationContext());
        }
        //内存溢出
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        //绑定服务
        bindService();

        //日志配置
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
//                .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("[[[ whx ]]]")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });

        //加载模型
//        GFace.SetKey("B052C8C5A91673242EFB");
//        boolean checkLicense = I2Cdm.check_dm_license(null);
        int res = GFace.loadModel(FILE_FACE6 + "/dnew.dat", FILE_FACE6 + "/anew.dat", FILE_FACE7 + "/db.dat", FILE_FACE7 + "/p.dat");
//        Logger.e("key: "+ GFace.GetSn("TS"));
//        Logger.d("设置地址：" + checkLicense);
        Logger.d("模型加載狀態：" + res);

        //线程池初始化
//        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);
//        scheduledThreadPool.schedule(runnable对象, 2000, TimeUnit.MILLISECONDS);
        ThreadPoolUtils.init(getNumberOfCPUCores() * 2);

        //初始化AndroidUtilCode
        Utils.init(application);
    }


    public void bindService() {
        bindService(new Intent(this, WhiteListService.class), whiteListConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection whiteListConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            whiteListService = ((WhiteListService.WhiteListBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            whiteListService = null;
        }
    };


    @Override
    public void onTerminate() {
        super.onTerminate();
//        shouldRun = false;
        unbindService(whiteListConnection);
    }

    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;  //上面的意思就是2.3以前不支持多核,有些特殊的设备有双核...不考虑,就当单核!!
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException | NullPointerException e) {
            cores = 0;   //这个常量得自己约定
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            //regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };
}
