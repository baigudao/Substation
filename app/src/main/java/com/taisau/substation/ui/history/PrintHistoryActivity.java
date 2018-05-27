package com.taisau.substation.ui.history;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.taisau.substation.R;
import com.taisau.substation.SubstationApplication;
import com.taisau.substation.bean.History;
import com.taisau.substation.ui.BaseActivity;

public class PrintHistoryActivity extends BaseActivity {
    private TextView time;
    private ImageView cardImg, faceImg;
    private TextView idcard;
    private ImageView resImg;
    private TextView resInfo, resResult;
    private RequestOptions requestOptions = new RequestOptions().centerCrop();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_history);
        initView();
        initData();

    }

    public void initView() {

        findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tv_setting_title)).setText(R.string.history_info);
        time = (TextView) findViewById(R.id.print_time);
        cardImg = (ImageView) findViewById(R.id.print_card_img);
        faceImg = (ImageView) findViewById(R.id.print_face_img);
        idcard = (TextView) findViewById(R.id.print_info_card2);
        resImg = (ImageView) findViewById(R.id.print_compare_info_img);
        resInfo = (TextView) findViewById(R.id.print_compare_info_res);
        resResult = (TextView) findViewById(R.id.print_compare_info_result);
    }

    public void initData() {
        long his_id = getIntent().getLongExtra("his_id", 0);
        History historyList = SubstationApplication.getApplication().getDaoSession().getHistoryDao().load(his_id);
        String date = getResources().getString(R.string.time_instance);
        String date2 = String.format(date, historyList.getTime());
        time.setText(date2);
        Glide.with(PrintHistoryActivity.this).load(historyList.getTemplatePhotoPath())
                .apply(requestOptions).into(cardImg);
//        cardImg.setImageBitmap(BitmapFactory.decodeFile(historyList.getTemplatePhotoPath()));
        Glide.with(PrintHistoryActivity.this).load(historyList.getFace_path())
                .apply(requestOptions).into(faceImg);
//        faceImg.setImageBitmap(BitmapFactory.decodeFile(historyList.getFace_path()));
        idcard.setText(historyList.getIc_card());

        if (historyList.getResult() == 0) {
            String score = getResources().getString(R.string.score_instance);
            String score2 = String.format(score, historyList.getScore());
            resInfo.setText(score2);
            resResult.setText(getString(R.string.compare_pass));
        } else {
            resImg.setImageResource(R.mipmap.his_compare_error_man);
            resInfo.setTextColor(getResources().getColor(R.color.color_f73030));
            resInfo.setText(getString(R.string.person_ic_no_accord));
            resResult.setTextColor(getResources().getColor(R.color.color_f73030));
            resResult.setText(getString(R.string.compare_fail));
        }
    }
}
