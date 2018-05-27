package com.taisau.substation.ui.setting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.taisau.substation.R;
import com.taisau.substation.em.DialogCase;
import com.taisau.substation.ui.BaseActivity;
import com.taisau.substation.ui.WelcomeActivity;
import com.taisau.substation.ui.setting.compare.CompareSettingActivity;
import com.taisau.substation.ui.setting.device.DeviceSettingActivity;
import com.taisau.substation.ui.setting.display.DisplaySettingActivity;
import com.taisau.substation.ui.setting.listener.OnDownLoadListener;
import com.taisau.substation.ui.setting.network.NetworkSettingActivity;
import com.taisau.substation.ui.setting.presenter.SettingPresenter;
import com.taisau.substation.ui.setting.view.ISettingView;

import java.io.File;
import java.io.IOException;

import static com.taisau.substation.util.Constant.LIB_DIR;

public class SettingActivity extends BaseActivity implements View.OnClickListener, ISettingView {

    private SettingPresenter presenter = new SettingPresenter(this);
    private TextView appVersion;
    private ImageView iconNewVersion;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String version = "";
        try {
            version = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version != null)
            presenter.checkVersion(version);
        updateAppVersion(version);
    }

    public void initView() {
        findViewById(R.id.rl_display_setting).setOnClickListener(this);//设备设置......
        findViewById(R.id.rl_compare_setting).setOnClickListener(this);//比对设置
        findViewById(R.id.rl_device_setting).setOnClickListener(this);//设备设置
        findViewById(R.id.rl_network_setting).setOnClickListener(this);//网络设置......
        findViewById(R.id.rl_update_setting).setOnClickListener(this);//版本更新设置
        findViewById(R.id.rl_restore_default).setOnClickListener(this);//恢复出厂设置......
        findViewById(R.id.rl_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_setting_title)).setText(R.string.setting);
        iconNewVersion = (ImageView) findViewById(R.id.iv_new_version);//新版本号
        appVersion = (TextView) findViewById(R.id.tv_app_version);//app版本
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.rl_display_setting:
                startActivity(new Intent(SettingActivity.this, DisplaySettingActivity.class));
                break;
            case R.id.rl_compare_setting:
                startActivity(new Intent(SettingActivity.this, CompareSettingActivity.class));
                break;
            case R.id.rl_device_setting:
                startActivity(new Intent(SettingActivity.this, DeviceSettingActivity.class));
                break;
            case R.id.rl_network_setting:
                startActivity(new Intent(SettingActivity.this, NetworkSettingActivity.class));//网络设置页面
                break;
            case R.id.rl_update_setting:
                if (iconNewVersion.getVisibility() == View.VISIBLE){
                    setAlertDialogShow(DialogCase.UPDATE_DIALOG);
                }else{
                   toastMsg("未检测到新版本");
                }
                break;
            case R.id.rl_restore_default:
                setAlertDialogShow(DialogCase.WARNING_DIALOG);
                break;
        }
    }

    @Override
    public void updateAppVersion(final String version) {//有新版本就传filePath
        appVersion.setText(version);
    }

    @Override
    public void setRestoreDefaultSuccess() {
      toastMsg(getString(R.string.restore_default_value_success));
    }


    @Override
    public void setAlertDialogShow(DialogCase dialogCase) {
        switch (dialogCase) {
            case UPDATE_DIALOG:

                break;
            case WARNING_DIALOG:
                new AlertDialog.Builder(SettingActivity.this).setTitle(R.string.recovery_default_value)
                        .setMessage(R.string.init_warn_info)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                presenter.clearData();
                                progressDialog = new ProgressDialog(SettingActivity.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("正在初始化...");
                            progressDialog.show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
								 hideSystemUi();
                            }
                        }).show();
                break;
        }
    }

    @Override
    public void toastMsg(String msg) {
        Toast.makeText(SettingActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearDataComplete() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.cancel();
            }
            Intent intent =   new Intent(SettingActivity.this, WelcomeActivity.class);
            intent.putExtra("exit_flag", "exit_id");
            startActivity(intent);
        });

    }

    private OnDownLoadListener listener = new OnDownLoadListener() {
        @Override
        public void onDownloadFinish() {
            String path = LIB_DIR + "/FaceCompare.apk";
            installAPK(Uri.fromFile(new File(path)));
        }

        @Override
        public void onDownloadFail(String msg) {

        }
    };

    private void installAPK(Uri uri) {
    /*    Uri packageURI = Uri.parse("package:com.taisau.substation");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);*/
        // 通过Intent安装APK文件
        try {
            String[] command = {"chmod", "777", LIB_DIR + "/FaceCompare.apk"};
            ProcessBuilder builder1 = new ProcessBuilder(command);
            builder1.start();
        //    ShellUtils.execCommand("pm install -r mnt/internal_sd/caffe_mobile/FaceCompare.apk",false);
           /* Runtime.getRuntime().exec("sh");
            Runtime.getRuntime().exec("pm uninstall com.taisau.substation");
            Runtime.getRuntime().exec("sh pm install -r mnt/internal_sd/caffe_mobile/FaceCompare.apk");
            Runtime.getRuntime().exec("sh reboot");*/
         /*   builder2.command(new String[]{"pm install -r "+LIB_DIR + "/FaceCompare.apk"});
            builder2.start();*/
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        Intent intents = new Intent();
        intents.setAction(Intent.ACTION_VIEW);
        // 如果不加上这句的话在apk安装完成之后点击单开会崩溃
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intents.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intents);
        //System.exit(-1);
    }

}