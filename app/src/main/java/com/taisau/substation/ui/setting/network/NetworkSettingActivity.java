package com.taisau.substation.ui.setting.network;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.taisau.substation.R;
import com.taisau.substation.listener.OnServerSettingChangeListener;
import com.taisau.substation.ui.BaseActivity;
import com.taisau.substation.ui.setting.adapter.NetworkSettingAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSettingActivity extends BaseActivity implements INetworkSettingView {

    private RecyclerView network_setting_list;
    private ArrayList<String> nameList;
    private HashMap<String, String> map;
    private NetworkSettingAdapter networkSettingAdapter;
    private NetworkSettingPresenter networkSettingPresenter;
    private SparseArray<String> valueList;
    private int netType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_setting);
        networkSettingPresenter = new NetworkSettingPresenter(this, this);
//        netType=networkSettingPresenter.getNetConnectType();
//        if (netType==-1)
//            Toast.makeText(NetworkSettingActivity.this,"尚未连接网络，无法进行设置", Toast.LENGTH_SHORT).show();
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
        ((TextView) findViewById(R.id.tv_setting_title)).setText(getString(R.string.network_setting));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        network_setting_list = (RecyclerView) findViewById(R.id.setting_list);
        network_setting_list.setLayoutManager(manager);
//        findViewById(R.id.btn_network_login).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent= new Intent();
//                intent.setAction("android.intent.action.VIEW");
//                Uri content_url = Uri.parse("https://www.baidu.com");
//                intent.setData(content_url);
//                startActivity(intent);
//            }
//        });
    }

    private void initData() {
        nameList = new ArrayList<>();
        nameList.add(getString(R.string.ip_server));
        nameList.add(getString(R.string.port_server));
//        nameList.add(getString(R.string.ip_gate));
//        nameList.add(getString(R.string.port_gate));
    }

    @Override
    protected void onResume() {
        super.onResume();
        valueList = networkSettingPresenter.getCurrentAddress();
        networkSettingAdapter = new NetworkSettingAdapter(this, nameList, valueList);
        network_setting_list.setAdapter(networkSettingAdapter);
//        if (netType==9)
        networkSettingAdapter.setOnItemClickListener(new NetworkSettingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
//                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                final EditText ip_edit = new EditText(NetworkSettingActivity.this);
                ip_edit.setSingleLine(true);
                ip_edit.setHint(position == 0 || position == 2 ? getString(R.string.ip_format) : getString(R.string.port_format));
                ip_edit.setInputType(InputType.TYPE_CLASS_PHONE);
                new AlertDialog.Builder(NetworkSettingActivity.this).setTitle(getString(R.string.setting) + nameList.get(position))
                        .setMessage(position == 0 || position == 2 ? (getString(R.string.ip_format_message)) : "")
                        .setView(ip_edit)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                networkSettingPresenter.setAddressChange(position, ip_edit.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });
//        else
//            Toast.makeText(NetworkSettingActivity.this,"只有以太网才可以设置网络参数", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void showChangeResult(int position, String address) {
        if (address != null) {
            Toast.makeText(NetworkSettingActivity.this, R.string.setting_success, Toast.LENGTH_SHORT).show();
            networkSettingAdapter.updateAddress(position, address);
            networkSettingAdapter.notifyDataSetChanged();
            OnServerSettingChangeListener.OnSettingChange(address);
        } else {
            Toast.makeText(NetworkSettingActivity.this, R.string.setting_fail, Toast.LENGTH_SHORT).show();
        }
    }
}
