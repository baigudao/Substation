package com.taisau.substation.ui.setting.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.ui.BaseActivity;
import com.taisau.substation.util.Constant;
import com.taisau.substation.util.FileUtils;

import java.io.File;
import java.util.ArrayList;


public class DisplaySettingActivity extends BaseActivity implements IDisplaySettingView, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView userName, mainTitle, sutTitle, adsPath;
    private String changeText = "";
    private ImageView adsImg;
    private DisplaySettingPresenter presenter;
    private String uploadPath;
    private AlertDialog dialog;
    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private static final String TAG = "DisplaySettingActivity";
    private RadioButton rbDoorwayIn, rbDoorwayOut;
    private RequestOptions requestOptions = new RequestOptions().fitCenter();
    private RequestOptions requestOptions2 = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).fitCenter().placeholder(R.drawable.ic_no_picture);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_setting);
        presenter = new DisplaySettingPresenter(this);
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        Logger.d("onCreate: point.width=" + point.x + ",point.y=" + point.y);//768*976
        ArrayList<String> contents = presenter.getDisplayContent();
        initView(contents);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initView(ArrayList<String> contents) {
        userName = (TextView) findViewById(R.id.tv_user_name);
        userName.setText(contents.get(0));
        mainTitle = (TextView) findViewById(R.id.tv_main_title);
        mainTitle.setText(contents.get(1));
        sutTitle = (TextView) findViewById(R.id.tv_subtitle);
        sutTitle.setText(contents.get(2));
        adsPath = (TextView) findViewById(R.id.tv_choose_picture_status);
        adsImg = (ImageView) findViewById(R.id.setting_display_ads_img);
        findViewById(R.id.btn_restore_default_picture).setOnClickListener(this);
        if (contents.get(3) != null && new File(contents.get(3)).exists()) {
            Glide.with(this).load(contents.get(3)).apply(requestOptions2).into(adsImg);
            adsPath.setText(contents.get(3));
            findViewById(R.id.btn_restore_default_picture).setClickable(true);
        } else {
            Glide.with(this).load(R.drawable.ic_no_picture).apply(requestOptions).into(adsImg);
            findViewById(R.id.btn_restore_default_picture).setClickable(false);
        }
        rbDoorwayIn = (RadioButton) findViewById(R.id.rb_doorway_in);
        rbDoorwayOut = (RadioButton) findViewById(R.id.rb_doorway_out);
        if (contents.get(4).equals("1")) {
            rbDoorwayIn.setChecked(true);
        } else {
            rbDoorwayOut.setChecked(true);
        }
        rbDoorwayIn.setOnCheckedChangeListener(this);
        rbDoorwayOut.setOnCheckedChangeListener(this);
        findViewById(R.id.rl_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_setting_title)).setText(getString(R.string.device_setting));
        findViewById(R.id.tv_user_name).setOnClickListener(this);
        findViewById(R.id.tv_main_title).setOnClickListener(this);
        findViewById(R.id.tv_subtitle).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.btn_upload).setOnClickListener(this);
        findViewById(R.id.btn_usb_insert).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_user_name:
                changeTextDialog(0);
                break;
            case R.id.tv_main_title:
                changeTextDialog(1);
                break;
            case R.id.tv_subtitle:
                changeTextDialog(2);
                break;
            case R.id.btn_search:
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;
            case R.id.btn_upload:
                if (uploadPath == null) {
                    Toast.makeText(DisplaySettingActivity.this, "请选择一张图片", Toast.LENGTH_SHORT).show();
                } else {
                    presenter.uploadPicture(uploadPath);
                }
                break;
            case R.id.btn_restore_default_picture:
                presenter.restoreDefaultPicture();
                break;
            case R.id.btn_usb_insert:
                presenter.getUsbAdPicture();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
//            Log.d(TAG, "onActivityResult: Cursor=" + c);
            if (c != null) {
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
//                Log.d(TAG, "onActivityResult: imagePath=" + imagePath);
                String end = imagePath.substring(imagePath.indexOf("."));
                File copyFile = new File(Constant.ADS_IMG + "/FaceCom_Ads" + end);
                FileUtils.copyFile(new File(imagePath), copyFile, true);
//                Log.d(TAG, "onActivityResult: copyFile.getAbsolutePath()=" + copyFile.getAbsolutePath());
                adsPath.setText(copyFile.getAbsolutePath());
                presenter.setDisplayContentChange(3, copyFile.getAbsolutePath());
                showImage(copyFile.getAbsolutePath());
                c.close();
            }
        }
    }


    //加载图片
    private void showImage(String imgPath) {
        if (new File(imgPath).exists()) {
            // 选中图片无需提示,需上传后才能在主页显示
            Glide.with(DisplaySettingActivity.this).load(imgPath).apply(requestOptions2).into(adsImg);
            uploadPath = imgPath;
            findViewById(R.id.btn_restore_default_picture).setClickable(true);
        } else {
            Glide.with(DisplaySettingActivity.this).load(R.drawable.ic_no_picture).apply(requestOptions).into(adsImg);
            uploadPath = null;
        }
    }


    private void changeTextDialog(final int position) {
        String title = "";
        switch (position) {
            case 0:
                title = getString(R.string.device_location);
                break;
            case 1:
                title = "主标题";
                break;
            case 2:
                title = "副标题";
                break;

        }
        final EditText editText = new EditText(DisplaySettingActivity.this);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        if (position != 0) {
            InputFilter[] filters = {new InputFilter.LengthFilter(20)};
            editText.setFilters(filters);
            editText.setHint("最长 20 个字");
        } else {
            InputFilter[] filters = {new InputFilter.LengthFilter(10)};
            editText.setFilters(filters);
            editText.setHint("最长 10 个字");
        }
        new AlertDialog.Builder(DisplaySettingActivity.this).setTitle(getString(R.string.setting) + title)
                .setMessage(R.string.tips_input_limit)
                .setView(editText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeText = editText.getText().toString();
                        presenter.setDisplayContentChange(position, changeText);
                        hideSystemUi();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideSystemUi();
                    }
                }).show();
    }

    @Override
    public void showDialog() {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(DisplaySettingActivity.this)
                    .setTitle("提示")
                    .setMessage("正在上传广告图片...")
                    .show();
        }
    }

    @Override
    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void showChangeResult(int position, boolean isSuccess) {
//        Log.d(TAG, "showChangeResult: position="+position+",isSuccess="+isSuccess );
        if (isSuccess) {
            Toast.makeText(DisplaySettingActivity.this, R.string.setting_success, Toast.LENGTH_SHORT).show();
            switch (position) {
                case 0:
                    userName.setText(changeText);
                    break;
                case 1:
                    mainTitle.setText(changeText);
                    break;
                case 2:
                    sutTitle.setText(changeText);
                    break;
            }
        } else {
            Toast.makeText(DisplaySettingActivity.this, R.string.setting_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showUploadResult(boolean isSuccess, String result) {
        if (isSuccess) {
            Toast.makeText(DisplaySettingActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
//            presenter.setDisplayContentChange(3, uploadPath);

            uploadPath = null;
        } else {
            Toast.makeText(DisplaySettingActivity.this, "上传失败，原因：" + result, Toast.LENGTH_SHORT).show();
            adsPath.setText("未选择图片");
            Glide.with(DisplaySettingActivity.this).load(R.drawable.ic_no_picture).apply(requestOptions).into(adsImg);
        }
    }

    @Override
    public void showRestoreDefaultPictureResult(boolean isSuccess, String result) {
        if (isSuccess) {
            Toast.makeText(DisplaySettingActivity.this, "恢复默认成功", Toast.LENGTH_SHORT).show();
            adsPath.setText("未选择图片");
            Glide.with(DisplaySettingActivity.this).load(R.drawable.ic_no_picture).apply(requestOptions).into(adsImg);
            findViewById(R.id.btn_restore_default_picture).setClickable(false);
        } else {
            Toast.makeText(DisplaySettingActivity.this, "恢复默认失败，原因：" + result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showUsbAdPicture(String adPath) {
        Logger.d(TAG, "showUsbAdPicture: adPath=" + adPath);
        if (adPath == null) {
            Toast.makeText(DisplaySettingActivity.this, "未检测到 U 盘，无法导入", Toast.LENGTH_SHORT).show();
        } else if (adPath != "" && new File(adPath).exists()) {
            presenter.setDisplayContentChange(3, adPath);
            Glide.with(DisplaySettingActivity.this).load(adPath).apply(requestOptions2).into(adsImg);
            findViewById(R.id.btn_restore_default_picture).setClickable(true);
        } else {
            Toast.makeText(DisplaySettingActivity.this, "图片不存在或图片异常无法加载", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rb_doorway_in:
                if (isChecked) {
                    presenter.setDisplayContentChange(4, "1");//1为入口  2为出口
                }
                break;
            case R.id.rb_doorway_out:
                if (isChecked) {
                    presenter.setDisplayContentChange(4, "2");//1为入口  2为出口
                }
                break;
        }
    }
}
