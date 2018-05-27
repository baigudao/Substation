package com.taisau.substation.ui.setting.compare;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.ui.BaseActivity;
import com.taisau.substation.widget.RegionNumberEditText;

import java.util.ArrayList;

public class CompareSettingActivity extends BaseActivity implements ICompareSettingView, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "CompareSettingActivity";
    private RelativeLayout ageWarningRelativeLayout, diySeekBarRelativeLayout, diyValueRelativeLayout;
    RegionNumberEditText minEt, maxEt, scoreEt;
    SeekBar seekBar;
    private CompareSettingPresent present;
    ArrayList<String> contents;
    RadioButton easy, hard, custom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_setting);
        present = new CompareSettingPresent(this);
        initData();
        initView();

    }

    private void initView() {
        findViewById(R.id.rl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.tv_setting_title)).setText(getResources().getString(R.string.compare_setting));
        ageWarningRelativeLayout = (RelativeLayout) findViewById(R.id.rl_age_warning);
        diySeekBarRelativeLayout = (RelativeLayout) findViewById(R.id.rl_diy_seekbar);
        diyValueRelativeLayout = (RelativeLayout) findViewById(R.id.rl_diy_value);
        Switch black = (Switch) findViewById(R.id.switch_black_list);
        Switch alive = (Switch) findViewById(R.id.switch_is_alive);
        Switch whether = (Switch) findViewById(R.id.switch_is_compare);
        Switch voice = (Switch) findViewById(R.id.switch_check_face_tts);
        Switch age = (Switch) findViewById(R.id.switch_age_warning);
        minEt = (RegionNumberEditText) findViewById(R.id.et_min_age);
        minEt.setRegion(999, 0);
//        minEt.setTextWatcher();
        maxEt = (RegionNumberEditText) findViewById(R.id.et_max_age);
        maxEt.setRegion(999, 0);
//        maxEt.setTextWatcher();
        black.setOnCheckedChangeListener(this);
        alive.setOnCheckedChangeListener(this);
        whether.setOnCheckedChangeListener(this);
        voice.setOnCheckedChangeListener(this);
        age.setOnCheckedChangeListener(this);
        maxEt.addTextChangedListener(watcher);
        minEt.addTextChangedListener(watcher);
        easy = (RadioButton) findViewById(R.id.rb_easy_compare);
        hard = (RadioButton) findViewById(R.id.rb_hard_compare);
        custom = (RadioButton) findViewById(R.id.rb_diy_compare);
        easy.setOnCheckedChangeListener(this);
        hard.setOnCheckedChangeListener(this);
        custom.setOnCheckedChangeListener(this);
        scoreEt = (RegionNumberEditText) findViewById(R.id.et_diy_value);
        scoreEt.setRegion(100, 0);
        scoreEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Logger.e(TAG, "自定义: s="+s.toString() );
                if (s.toString().equals("")) {
                    present.setCompareContentChange(5,"0");
                    seekBar.setProgress(0);
                }else{
                    present.setCompareContentChange(5, s.toString());
                    seekBar.setProgress(Integer.parseInt(s.toString()));
                }

            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBar_setting);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String content  = ""+seekBar.getProgress();
                scoreEt.setText(content);
                present.setCompareContentChange(5, "" + seekBar.getProgress());
            }
        });

//        if (contents.get(0).equals("true"))
//            black.setChecked(true);
//        else
//            black.setChecked(false);
        if (contents.get(1).equals("true"))
            alive.setChecked(true);
        else
            alive.setChecked(false);
        if (contents.get(2).equals("true"))
            voice.setChecked(true);
        else
            voice.setChecked(false);
        if (contents.get(3).equals("true")) {
            age.setChecked(true);
            ageWarningRelativeLayout.setVisibility(View.VISIBLE);
            present.showAgeRange();
        } else {
            age.setChecked(false);
            ageWarningRelativeLayout.setVisibility(View.GONE);
        }
        if (contents.get(4).equals("easy")) {
            easy.setChecked(true);
        } else if (contents.get(4).equals("hard")) {
            hard.setChecked(true);
        } else {
            custom.setChecked(true);
        }
//        Log.e(TAG, "initView: contents.get(4)="+contents.get(4) );
//        Log.e(TAG, "initView: contents.get(5)="+contents.get(5) );
        scoreEt.setText(contents.get(5));
        seekBar.setProgress(Integer.parseInt(contents.get(5)));
        if (contents.get(6).equals("true")) {
            whether.setChecked(true);
        } else {
            whether.setChecked(false);
        }
    }

    public void initData() {
        contents = present.getCompareContent();
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (minEt.isFocused()) {//正在编辑最小值
//                Log.e(TAG, "afterTextChanged:   minEt");
                if (s.toString().equals("")) {
//                    minEt.setText("" + minEt.getRegion()[1]);
//                    Log.e(TAG, "afterTextChanged:   清空，设置最小值");
                    present.setAgeRange("" + minEt.getRegion()[0], "");
                }else{
                    present.setAgeRange(minEt.getText().toString(), "");
                }

            } else {//正在编辑最大值
//                Log.e(TAG, "afterTextChanged:   maxEt");
                if (s.toString().equals("")) {
//                    maxEt.setText("" + maxEt.getRegion()[1]);
//                    Log.e(TAG, "afterTextChanged:    清空，设置最小值");
                    present.setAgeRange("", ""+maxEt.getRegion()[1]);
                }else{
                    present.setAgeRange("", maxEt.getText().toString());
                }
            }

        }
    };

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void showDiyLayout(boolean show) {
        diySeekBarRelativeLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        diyValueRelativeLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.switch_black_list:
                if (b)
                    present.setCompareContentChange(0, "true");
                else
                    present.setCompareContentChange(0, "false");
                break;
            case R.id.switch_is_alive:
                if (b)
                    present.setCompareContentChange(1, "true");
                else
                    present.setCompareContentChange(1, "false");
                break;
            case R.id.switch_check_face_tts:
                if (b)
                    present.setCompareContentChange(2, "true");
                else
                    present.setCompareContentChange(2, "false");
                break;
            case R.id.switch_age_warning:
                if (b) {
                    present.setCompareContentChange(3, "true");
                    ageWarningRelativeLayout.setVisibility(View.VISIBLE);
                    present.showAgeRange();
                } else {
                    present.setCompareContentChange(3, "false");
                    ageWarningRelativeLayout.setVisibility(View.GONE);

                }
                break;
            case R.id.rb_easy_compare:
                if (b) {
                    present.setCompareContentChange(4, "easy");
                }
                break;
            case R.id.rb_hard_compare:
                if (b) {
                    present.setCompareContentChange(4, "hard");
                }
                break;
            case R.id.rb_diy_compare:
                if (b) {
                    present.setCompareContentChange(4, "diy");
                }
                showDiyLayout(b);
                break;
            case R.id.switch_is_compare:
                if (b)
                    present.setCompareContentChange(6, "true");
                else
                    present.setCompareContentChange(6, "false");
                break;
        }
    }

    @Override
    public String getCurrentDisplayContent(int flag) {
        return null;
    }

    @Override
    public void setDisplayContentChange(int pos, String content) {

    }

    @Override
    public void showAgeRange(String min, String max) {
        minEt.setText(min);
        maxEt.setText(max);
    }
}
