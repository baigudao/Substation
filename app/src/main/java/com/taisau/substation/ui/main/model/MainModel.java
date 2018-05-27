package com.taisau.substation.ui.main.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.GFace;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.SubstationApplication;
import com.taisau.substation.bean.Person;
import com.taisau.substation.listener.OnCardDetectListener;
import com.taisau.substation.listener.OnFaceDetectListener;
import com.taisau.substation.listener.OnUpdateScoreListener;
import com.taisau.substation.listener.SaveAndUpload;
import com.taisau.substation.service.WhiteListService;
import com.taisau.substation.ui.main.MainActivity;
import com.taisau.substation.ui.main.contract.MainContract;
import com.taisau.substation.ui.main.utils.FeaAction;
import com.taisau.substation.util.FeaUtils;
import com.taisau.substation.util.ImgUtils;
import com.taisau.substation.util.Preference;
import com.taisau.substation.util.SoundUtils;
import com.taisau.substation.util.ThreadPoolUtils;
import com.taisau.substation.util.YUVUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.taisau.substation.util.Constant.FACE_IMG;

//import java.text.DateFormat;


/**
 * Created by whx on 2017-09-04
 */

public class MainModel implements MainContract.Model, Camera.PreviewCallback, OnCardDetectListener, OnFaceDetectListener, OnUpdateScoreListener {

    private StringBuffer time = new StringBuffer();
    private MainContract.Presenter presenter;
    private static boolean isRun = false;
    private static int noFaceCount = 0;
    //是否进行人脸比对
    public static volatile boolean runDetect = false;
    //检测到人脸
    private static boolean hasFace = false;
    //是否进行人脸比对
    private static boolean runCompare = false;

    //检测后的数据
    private GFace.FaceInfo aa;

    //    private float[] modFea;
    private List<float[]> modFeasList;

    //历史信息Model
    private static long historyID;
    private static long useElectricRecordID;

    private float comScore = 65;

    private static int noFace = /*20*/1;

    private volatile int disCount = 0;

    private FeaAction feaAction = new FeaAction();

    private volatile boolean cpuSleeping = false;

    private Handler handler = new Handler();


    private Thread updateTimeThread;

    private volatile boolean isSlow = false;
    private long testStartTime;

    public MainModel(/*Context context,*/ MainContract.Presenter presenter2) {
        this.presenter = presenter2;
//        this.mContext = context;
//        initSoundPool();
    }

    public Camera.PreviewCallback getPreviewCallback() {
        return MainModel.this;
    }

    //    @Override
    public OnCardDetectListener getCardDetectListener() {
        return MainModel.this;
    }


    @Override
    public void changeSound(boolean isInit, int soundNum) {
        int current = SoundUtils.getInstance().getCurrentVolume(AudioManager.STREAM_MUSIC);
        switch (soundNum) {
            case 0: //静音
                SoundUtils.getInstance().muteSound(AudioManager.STREAM_MUSIC);
                Preference.setDevVolume("0");
                break;
            case -1: //音量 +1
                SoundUtils.getInstance().addSound(AudioManager.STREAM_MUSIC);
                current += 1;
                Preference.setDevVolume(String.valueOf(current));
                break;
            case -2: //音量 -1
                SoundUtils.getInstance().decreaseSound(AudioManager.STREAM_MUSIC);
                current -= 1;
                Preference.setDevVolume(String.valueOf(current));
                break;
            case -3: //音量最大
                SoundUtils.getInstance().maxSound(AudioManager.STREAM_MUSIC);
                int max = SoundUtils.getInstance().getMaxVolume(AudioManager.STREAM_MUSIC);
                Preference.setDevVolume(String.valueOf(max));
                break;
            default: //直接设置音量
                if (isInit) {
                    SoundUtils.getInstance().setSoundMute(AudioManager.STREAM_MUSIC, soundNum);
                } else {
                    SoundUtils.getInstance().setSound(AudioManager.STREAM_MUSIC, soundNum);
                }
                Preference.setDevVolume(String.valueOf(soundNum));
                break;
        }

    }

    @Override
    public void setRunDetect(boolean run) {
        runDetect = run;
        presenter.updateFaceFrame(changeSituation(0, 0, 0, 0), 640, 360);
    }

    @Override
    public void onUpdateScore(float score) {
        comScore = score;
    }

    private void initTime() {
        isRun = true;
        runDetect = true;
//        CompareService.setOnCardDetectListener(this);
        WhiteListService.setOnCardDetectListener(this);

        MainActivity.setOnFaceDetectListener(this);//MainActivity中的監聽
        WhiteListService.setOnUpdateScoreListener(this);


        initSetting();
//        if (WC) {
//            initWhiteFea();
//        }
        //  2017/09/07 星期四 15:27:50
        //Logger.e(TAG, "onResume: 年 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR) );//2017
        // Logger.e(TAG, "onResume: 月 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.MONTH) );//8
        //Logger.e(TAG, "onResume: 日 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_MONTH) );//7
        // Logger.e(TAG, "onResume: 星期 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_WEEK) );//5
        // Logger.e(TAG, "onResume: 日期 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.DATE) );//7
        // Logger.e(TAG, "onResume: 时 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.HOUR) );//3
        // Logger.e(TAG, "onResume: 分 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.MINUTE) );//27
        //Logger.e(TAG, "onResume: 秒 = "+Calendar.getInstance(Locale.CHINA).get(Calendar.SECOND) );//50
        updateTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        while (isRun) {
                            time.delete(0, time.length());

                            Date curDate = new Date(System.currentTimeMillis());
                            switch (curDate.getDay()) {
                                case 1:
                                    time.append("星期一 ");
                                    break;
                                case 2:
                                    time.append("星期二 ");
                                    break;
                                case 3:
                                    time.append("星期三 ");
                                    break;
                                case 4:
                                    time.append("星期四 ");
                                    break;
                                case 5:
                                    time.append("星期五 ");
                                    break;
                                case 6:
                                    time.append("星期六 ");
                                    break;
                                case 0:
                                    time.append("星期日 ");
                                    break;
                            }
                            time.append(curDate.getHours()).append(":");
                            if (curDate.getMinutes() < 10)
                                time.append("0").append(curDate.getMinutes());
                            else
                                time.append(curDate.getMinutes());

                            handler.post(() -> presenter.updateTimeToView(time.toString()));
                            //等待
                            wait((60 - curDate.getSeconds()) * 1000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //页面退出后需要中断线程
                }
            }
        });
        updateTimeThread.start();
    }

    private void stopTime() {
        isRun = false;
        runDetect = false;
        stopThread(updateTimeThread);

    }

    private void stopThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    private void initSetting() {
        if (Preference.getHisLastId() != null && !Preference.getHisLastId().equals(""))
            historyID = Long.parseLong(Preference.getHisLastId()) + 1;
        else
            historyID = 1;
//        if (Preference.getScoreRank() != null && !Preference.getScoreRank().equals(""))
//            switch (Preference.getScoreRank()) {
//                case "easy":
//                    comScore = 65;
//                    break;
//                case "hard":
//                    comScore = 75;
//                    break;
//                default:
//                    comScore = Float.parseFloat(Preference.getScoreRankValue());
//                    break;
//            }
        if (Preference.getHisLastId() != null && !Preference.getHisLastId().equals(""))
            useElectricRecordID = Long.parseLong(Preference.getHisLastId()) + 1;
        else
            useElectricRecordID = 1;
        comScore = Preference.getOvoCompareThreshold() == null ?
                85 : Float.parseFloat(Preference.getOvoCompareThreshold());
        if (Preference.getNoFaceCount() != null && !Preference.getNoFaceCount().equals("")) {
            noFace = Integer.parseInt(Preference.getNoFaceCount());
        }
    }


    @Override
    public String getAdsTitle() {
        return Preference.getMainTittle();
    }

    @Override
    public String getAdsSubtitle() {
        return Preference.getSubTittle();
    }

    @Override
    public String[] getAdsImagePath() {
        // /mnt/internal_sd/DCIM/Camera/IMG_20170907_131825.jpg
        // /mnt/internal_sd/DCIM/Camera/IMG_20170907_110757.jpg
        return new String[]{Preference.getAdsPath()/*,
        "/mnt/internal_sd/DCIM/Camera/IMG_20170907_131825.jpg",
        "/mnt/internal_sd/DCIM/Camera/IMG_20170907_110757.jpg"*/
        };
    }

    @Override
    public String getUserName() {
        return Preference.getCustomName();
    }


    @Override
    public void startUpdateTime() {
        initTime();
//        if (WhiteListService.personCache != null) {
//            onDetectCard(WhiteListService.personCache);
//            WhiteListService.personCache = null;
//        }
    }

    @Override
    public void stopUpdateTime() {
        WhiteListService.setOnCardDetectListener(null);
        stopTime();
    }


    private void clearMode() {
        noFaceCount = 0;
        presenter.setCompareLayoutVisibility(View.GONE);
        presenter.updateCompareResultScore("", View.GONE);
        presenter.updateCompareResultImg(-1, View.GONE);
        runCompare = false;
    }

    private String icCard;
    private String cardPath;

    private boolean isAfterNow(String dateStr) {
        if (dateStr.equals("null") || dateStr.equals("")) {
            return false;
        }
        Date now = new Date();
//        java.text.DateFormat dateFormat = SimpleDateFormat.getInstance();
//        dateFormat.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        try {
            Date date = simpleDateFormat.parse(dateStr);
            return date.after(now);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isBeforeNow(String dateStr) {
        if (dateStr.equals("null") || dateStr.equals("")) {
            return false;
        }
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
//        java.text.DateFormat dateFormat = SimpleDateFormat.getInstance();
        try {
            Date date = simpleDateFormat.parse(dateStr);
            return date.before(now);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onDetectCard(Person person) {//接收到IC卡的id,从数据库中取特征值
        testStartTime = System.currentTimeMillis();
        Observable.create((ObservableOnSubscribe<Person>) e -> {
//            Date enterDate = dateFormat.parse(person.getEnterDate());//受雇日期
//            Date leaveDate = dateFormat.parse(person.getLeaveDate());//离职日期
//            Date siteBeginDate = dateFormat.parse(person.getSiteBeginDate());//地点开始日期
//            Date siteEndDate = dateFormat.parse(person.getSiteEndDate());//地点结束日期
//            Date safetyCardExpiryDate = dateFormat.parse(person.getSafetyCardExpiryDate());//安全卡到期日期
            if (isAfterNow(person.getEnterDate()) || isAfterNow(person.getSiteBeginDate())
                    || isBeforeNow(person.getSafetyCardExpiryDate())
                    || isBeforeNow(person.getLeaveDate())
                    || isBeforeNow(person.getSiteEndDate())) {
                e.onComplete();
            } else {
                e.onNext(person);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(person1 -> {
//                            Logger.e("准备处理  耗时(毫秒)：" + (System.currentTimeMillis() - testStartTime));
                            isSlow = false;
                            presenter.updateCompareResultInfo("", Color.WHITE);


                            icCard = person.getIc_card();
                            cardPath = person.getImg_path();
                            Bitmap cardBit = BitmapFactory.decodeFile(cardPath);
                            if (cardPath == null || cardBit == null) {
                                Toast.makeText(SubstationApplication.getApplication(),
                                        SubstationApplication.getApplication().getResources().
                                                getString(R.string.template_picture_is_null),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                cardBit = Bitmap.createScaledBitmap(cardBit, 200, 300, false);
                                presenter.updateCompareRealRes(null);
                                presenter.updateCompareCardRes(cardBit);
                                presenter.updateCompareResultImg(-1, View.GONE);
                                presenter.updateCompareResultScore("start", View.GONE);


                                presenter.setCompareLayoutVisibility(View.VISIBLE);
                                noFaceCount = 0;

                                modFeasList = FeaUtils.getFeaList(person.getAllFeaPath());
                                presenter.updateCompareResultInfo(SubstationApplication.getApplication()
                                        .getString(R.string.take_picture_tip), Color.WHITE);
                                handler.postDelayed(() -> {
                                    runCompare = true;
                                }, 200);
                            }
                        }, Throwable::printStackTrace
                        , () -> Toast.makeText(SubstationApplication.getApplication(),
                                SubstationApplication.getApplication().getResources().
                                        getString(R.string.out_of_range), Toast.LENGTH_SHORT).show());
    }

    private List<Person> personList;
    private String inputContent;

    private Timer timer;
    private TimerTask timerTask;

    private void closeTimer() {
        timer.cancel();
        timer = null;
        timerTask.cancel();
        timerTask = null;
    }

    @Override
    public void onDetectFace(String inputContent) {

        testStartTime = System.currentTimeMillis();//記錄人臉比對開始時間
        setRunDetect(true);

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showLong("請重新比對！");
                        presenter.initDistributionBox();
                    }
                });
            }
        };
        timer.schedule(timerTask, 10000);

        this.inputContent = inputContent;
        //得到保存的数据
        QueryBuilder<Person> builder = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder();
        personList = builder.list();
        Logger.d("personList.size = " + personList.size());


        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                int personSize = personList.size();
                if (personSize == 0) {
                    Toast.makeText(SubstationApplication.getApplication(), "暫時無數據，無法比對！", Toast.LENGTH_LONG).show();
                    closeTimer();
                    presenter.initDistributionBox();
                    return;
                } else {
                    isSlow = false;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runCompare = true;
                        }
                    }, 200);
                }
            }
        });
    }

    private byte[] temp = null;
    private byte[] ret = null;
    private long[] situation = new long[4];

    private long[] changeSituation(long... situations) {
//        System.arraycopy(situations, 0, situation, 0, situations.length);
        situation[0] = situations[0];
        situation[1] = situations[1];
        situation[2] = situations[2];
        situation[3] = situations[3];
        return situation;
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (runDetect) {
            if (isSlow) {
                if (!cpuSleeping) {
                    cpuSleeping = true;
                } else {
                    return;
                }
                ThreadPoolUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                wait(700);
                                cpuSleeping = false;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
            //压缩yuv图片
            temp = YUVUtils.scaleYUV300_200(data, 1280, 720);
            ret = GFace.detectFace(temp, 300, 200);
            if (ret != null && ret[0] > 0) {
                aa = GFace.getFaceInfo(ret);
                aa = adjustFaceInfo(aa, true);
                //生成人脸框
//                if (SubstationApplication.getApplication().isOpenFaceFrame()) {
                presenter.updateFaceFrame(changeSituation(aa.rc[0].left, aa.rc[0].top
                        , aa.rc[0].right, aa.rc[0].bottom), 640, 360);
//                }
                hasFace = true;
                if (isSlow) {
                    isSlow = false;
                }
            } else {
//                presenter.updateFaceFrame(changeSituation(0, 0, 0, 0), 640, 360);//step1
                if (noFaceCount < noFace * 10)
                    noFaceCount++;
                if (noFaceCount == noFace * 10) {
                    clearMode();
                    if (!isSlow) {
                        isSlow = true;
                    }
                }
                hasFace = false;
            }
        }
        if (hasFace && runCompare) {
//            Logger.e("获取现场人脸  耗时(毫秒)：" + (System.currentTimeMillis() - testStartTime));
//            SubstationApplication.getApplication().setOpenFaceFrame(true);//是否開啟人臉框

            runCompare = false;
            noFaceCount = 0;
            byte[] bitmapByte = YUVUtils.saveYUV(data, 1280, 720);
            Bitmap faceBit = ImgUtils.getUtils().adjustBitmap(BitmapFactory.decodeByteArray(
                    bitmapByte, 0, bitmapByte.length), aa, 2);
            presenter.updateCompareRealRes(faceBit);
            String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()).toString();
            String facePath = FACE_IMG + "/" + android.text.format.DateFormat.format("yyyyMMdd_HH_mm_ss",
                    Calendar.getInstance(Locale.CHINA)).toString() + "_face_img.png";
            aa = adjustFaceInfo(aa, false);
            Logger.d("提取现场特征值  开始 耗时(毫秒)：" + (System.currentTimeMillis() - testStartTime));
            float[] com_fea = feaAction.doFeaAction(FeaAction.FEA_CASE.DO_FACE_COMPARE, temp, aa);
            Logger.d("提取现场特征值  结束 耗时(毫秒)：" + (System.currentTimeMillis() - testStartTime));


            HashMap<Float, Person> floatPersonHashMap = new HashMap<>();
            for (int i = 0; i < personList.size(); i++) {
                modFeasList = FeaUtils.getFeaList(personList.get(i).getAllFeaPath());
                float score = 0;
                if (com_fea != null) {
                    float temp;
                    for (float[] fea : modFeasList) {
                        temp = GFace.feaCompare(fea, com_fea);
                        if (temp > score) {
                            score = temp;
                        }
                    }
                }
                score = (float) (Math.round(score * 100)) / 100;
                Logger.d("score=" + score);
                floatPersonHashMap.put(score, personList.get(i));
            }
            Float aFloat = Collections.max(floatPersonHashMap.keySet());
            if (aFloat != 0.0) {
                Person person = floatPersonHashMap.get(aFloat);
                Logger.e("最大的分数值為：" + aFloat);

                //看看是否已經打開
                if (SPUtils.getInstance().getBoolean("isOpen" + inputContent)) {
                    //如果有打開，看是不是同一個人
                    if (person.getId() == SPUtils.getInstance().getLong(inputContent)) {
                        handlerCompareResult(aFloat, person, facePath, faceBit, date);//可能最大值有兩個，這就可能出現相片和活體不是一個人的問題
                    } else {
                        //不是同一個人關
                        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showLong("無權限關閉！");
                                closeTimer();
                                presenter.initDistributionBox();
                            }
                        });
                    }
                } else {
                    handlerCompareResult(aFloat, person, facePath, faceBit, date);//可能最大值有兩個，這就可能出現相片和活體不是一個人的問題
                }
                //                presenter.updateCompareResultInfo(SubstationApplication.getApplication().getResources().getString(R.string.ic_num) + icCard, Color.WHITE);
//                handleCompareResult(icCard, faceBit, date, facePath, cardPath, score, 2);
            } else {
                //如果最大值為0.0
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showLong("暫無匹配人員，請先註冊！");
                        closeTimer();
                        presenter.initDistributionBox();
                    }
                });
            }
        }
    }

    private GFace.FaceInfo adjustFaceInfo(GFace.FaceInfo aa, boolean isAdd) {
        if (isAdd) {
            aa.rc[0].left = aa.rc[0].left + 170;//越大，框越往左
            aa.rc[0].right = aa.rc[0].right + 170;
            aa.rc[0].top = aa.rc[0].top + 80;//越大，框越往下
            aa.rc[0].bottom = aa.rc[0].bottom + 80;
        } else {
            aa.rc[0].left = aa.rc[0].left - 170;
            aa.rc[0].right = aa.rc[0].right - 170;
            aa.rc[0].top = aa.rc[0].top - 80;
            aa.rc[0].bottom = aa.rc[0].bottom - 80;
        }
        return aa;
    }

    private void handleCompareResult(String icCard, Bitmap faceBit, String date, String facePath,
                                     String templatePhotoPath, float score, int compareType) {
        String mistakeValues;
//        boolean isBlack = person.getPerson_type() == 1;//白名单为0，黑名单为1

        int result = 1;
        if (score > comScore) {
            result = 0;
            String score1 = SubstationApplication.getApplication().getResources().getString(R.string.score_instance);
            String score2 = String.format(score1, score);
            presenter.updateCompareResultScore(score2, View.VISIBLE);
            presenter.updateCompareResultImg(R.mipmap.compare_pass, View.VISIBLE);
            mistakeValues = SubstationApplication.getApplication().getString(R.string.compare_pass_score) + score;
//            SubstationApplication.whiteListService.sendSerialPortMsg();//比对通过发送串口命令开启闸机
        } else {
            mistakeValues = SubstationApplication.getApplication().getString(R.string.compare_fail_score) + score;
            presenter.updateCompareResultImg(R.mipmap.compare_fail_error_man, View.VISIBLE);
            presenter.updateCompareResultScore(SubstationApplication.getApplication().getString(R.string.person_ic_no_accord), View.VISIBLE);
        }
        Logger.e("比对  耗时(毫秒)：" + (System.currentTimeMillis() - testStartTime));
        SaveAndUpload.OnSaveAndUpload(historyID, faceBit, templatePhotoPath, facePath, mistakeValues,
                score, date, icCard, result, compareType);

        historyID++;
        disCount++;
        handler.postDelayed(() -> {
            if (disCount == 1) {
                presenter.updateCompareCardRes(null);
                presenter.updateCompareRealRes(null);
                presenter.updateCompareResultScore("", View.GONE);
                presenter.updateCompareResultImg(-1, View.GONE);
                presenter.updateCompareResultInfo("", View.VISIBLE);
                presenter.setCompareLayoutVisibility(View.GONE);
            }
            disCount--;
        }, 1500);
    }

    private void handlerCompareResult(float score, Person person, String face_path, Bitmap faceBit, String date) {
        String mistakeValues;
//        boolean isBlack = person.getPerson_type() == 1;//白名单为0，黑名单为1

        String temple_image_path = person.getImg_path();//得到數據庫中的人臉模板
        Bitmap temple_image_bitmap = BitmapFactory.decodeFile(temple_image_path);
        if (temple_image_path == null || temple_image_bitmap == null) {
            Toast.makeText(SubstationApplication.getApplication(),
                    SubstationApplication.getApplication().getResources().
                            getString(R.string.template_picture_is_null),
                    Toast.LENGTH_SHORT).show();
        } else {
            temple_image_bitmap = Bitmap.createScaledBitmap(temple_image_bitmap, 200, 300, false);
//            presenter.updateCompareRealRes(null);//右邊的圖片
            presenter.updateCompareCardRes(temple_image_bitmap);//左邊的圖片
            presenter.updateCompareResultImg(-1, View.GONE);
            presenter.updateCompareResultScore("start", View.GONE);
        }
        //不顯示人臉框
        presenter.setRunDetect(false);
        //显示比对结果的界面
        presenter.setCompareLayoutVisibility(View.VISIBLE);

        noFaceCount = 0;
        int result = 1;
        Logger.e("比对的阈值为：" + comScore);
//        score = (float) 89.33;
        if (score > comScore) {//comScore=65  比对通过
            result = 0;
            String score1 = SubstationApplication.getApplication().getResources().getString(R.string.score_instance);
            String score2 = String.format(score1, score);
            presenter.updateCompareResultScore(score2, View.VISIBLE);
            presenter.updateCompareResultImg(R.mipmap.compare_pass, View.VISIBLE);
            mistakeValues = SubstationApplication.getApplication().getString(R.string.compare_pass_score) + score;
            SubstationApplication.whiteListService.sendAnotherSerialPortMsg(useElectricRecordID, inputContent, person, score, face_path, faceBit, date);//比对通过发送串口命令
        } else {
            mistakeValues = SubstationApplication.getApplication().getString(R.string.compare_fail_score) + score;
            presenter.updateCompareResultImg(R.mipmap.compare_fail_error_man, View.VISIBLE);
            presenter.updateCompareResultScore(SubstationApplication.getApplication().getString(R.string.person_ic_no_accord), View.VISIBLE);
        }
        Logger.e("比对  耗时(毫秒)：" + (System.currentTimeMillis() - testStartTime));
        //關閉timer
        closeTimer();
//        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()).toString();
//        SaveAndUpload.OnSaveAndUpload(useElectricRecordID, null, person.getImg_path(), face_path, mistakeValues,
//                score, date, "", result, 2);

        useElectricRecordID++;
        disCount++;
        handler.postDelayed(() -> {
            if (disCount == 1) {
                presenter.updateCompareCardRes(null);
                presenter.updateCompareRealRes(null);
                presenter.updateCompareResultScore("", View.GONE);
                presenter.updateCompareResultImg(-1, View.GONE);
                presenter.updateCompareResultInfo("", View.VISIBLE);
                presenter.setCompareLayoutVisibility(View.GONE);
                presenter.initDistributionBox();
            }
            disCount--;
        }, 3000);
    }
}
