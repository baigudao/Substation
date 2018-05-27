package com.taisau.substation.ui.history;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.SubstationApplication;
import com.taisau.substation.bean.History;
import com.taisau.substation.bean.HistoryDao;
import com.taisau.substation.ui.BaseActivity;
import com.taisau.substation.ui.history.adpter.HistoryAdapter;
import com.taisau.substation.util.ThreadPoolUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class HistoryListActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<History> dataList = new ArrayList<>();
    private List<History> showDataList = new ArrayList<>();
    private LinearLayoutManager manager;
    private int lastVisibleItem;
    private TextView showSize, allSize;
    private Handler handler = new Handler();
    private long lastInput;
    private long firstInput;
    private boolean isPrepare = false;
    private String search_content = "";
    private static int showLastItem;
    private TextView tvDataTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initView() {
        tvDataTips = (TextView) findViewById(R.id.activity_history_null);
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        findViewById(R.id.history_to_top).setOnClickListener(v -> recyclerView.scrollToPosition(0));
        recyclerView = (RecyclerView) findViewById(R.id.activity_history_list);
        manager = new LinearLayoutManager(HistoryListActivity.this);
        recyclerView.setLayoutManager(manager);

        allSize = (TextView) findViewById(R.id.activity_history_all_size);
        showSize = (TextView) findViewById(R.id.activity_history_show_size);

        String total = getResources().getString(R.string.history_count);
        String total2 = String.format(total, dataList.size());
        allSize.setText(total2);
        String show = getResources().getString(R.string.show_count);
        String show2 = String.format(show, showDataList.size());
        showSize.setText(show2);
        EditText search = (EditText) findViewById(R.id.activity_history_search_edit);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                lastInput = System.currentTimeMillis();
                search_content = s.toString();
                if (!isPrepare) {
                    isPrepare = true;
                    firstInput = System.currentTimeMillis();
                    ThreadPoolUtils.execute(searchCatcher);
                }
            }
        });
    }

    public void initData() {
        ThreadPoolUtils.execute(() -> {
            dataList = SubstationApplication.getApplication().getDaoSession().getHistoryDao().queryBuilder().orderDesc(HistoryDao.Properties.Time).list();
            if (dataList.size() <= 20) {
                showDataList = dataList;
                showLastItem = dataList.size();
            } else {
                showDataList = dataList.subList(0, 20);
                showLastItem = 20;
            }
            handler.post(this::updateData);
        });
    }

    private void updateData() {
        if (showDataList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            tvDataTips.setVisibility(View.GONE);
            adapter = new HistoryAdapter(HistoryListActivity.this, showDataList);
            recyclerView.setAdapter(adapter);
        } else {
            tvDataTips.setVisibility(View.VISIBLE);
            tvDataTips.setText(getResources().getString(R.string.no_data));
            recyclerView.setVisibility(View.GONE);
        }
        if (dataList.size() > 20)
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
                    Logger.d("on scroll state change");

                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                        if (showLastItem == dataList.size()) {
                            return;
                        }
                        adapter.changeMoreStatus(1, null);
                        handler.postDelayed(() -> {
                            if (showLastItem + 10 < dataList.size()) {
                                showLastItem = showLastItem + 10;
                                showDataList = dataList.subList(0, showLastItem);
                                adapter.changeMoreStatus(0, showDataList);
                            } else {
                                showLastItem = dataList.size();
                                adapter.changeMoreStatus(3, dataList);
                                showDataList = dataList;
                            }
                            recyclerView.scrollToPosition(adapter.getItemCount() - 8);
                            String total1 = getResources().getString(R.string.history_count);
                            String total21 = String.format(total1, dataList.size());
                            allSize.setText(total21);
                            String show1 = getResources().getString(R.string.show_count);
                            String show21 = String.format(show1, showDataList.size());
                            showSize.setText(show21);
                        }, 1000);
                    }
                }

                @Override
                public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                    lastVisibleItem = manager.findLastVisibleItemPosition();
                    if (lastVisibleItem + 1 == adapter.getItemCount()) {
                        if (showLastItem == dataList.size()) {
                            return;
                        }
                        adapter.changeMoreStatus(1, null);
                        handler.postDelayed(() -> {
                            if (showLastItem + 10 < dataList.size()) {
                                showLastItem = showLastItem + 10;
                                showDataList = dataList.subList(0, showLastItem);
                                adapter.changeMoreStatus(0, showDataList);
                            } else {
                                showLastItem = dataList.size();
                                adapter.changeMoreStatus(3, dataList);
                                showDataList = dataList;
                            }
                            recyclerView.scrollToPosition(adapter.getItemCount() - 8);
                            String total12 = getResources().getString(R.string.history_count);
                            String total212 = String.format(total12, dataList.size());
                            allSize.setText(total212);
                            String show12 = getResources().getString(R.string.show_count);
                            String show212 = String.format(show12, showDataList.size());
                            showSize.setText(show212);
                        }, 1000);
                    }
                }

            });
        String total = getResources().getString(R.string.history_count);
        String total2 = String.format(total, dataList.size());
        allSize.setText(total2);
        String show = getResources().getString(R.string.show_count);
        String show2 = String.format(show, showDataList.size());
        showSize.setText(show2);
    }

    Runnable searchCatcher = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                try {
                    while (isPrepare) {
                        wait(2000);
                        if (lastInput - firstInput <= 1500) {
                            doSearch(search_content);
                        } else {
                            firstInput = lastInput;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void doSearch(String search_content) {
        QueryBuilder builder1 = SubstationApplication.getApplication().getDaoSession().getHistoryDao().queryBuilder();
        builder1.whereOr(HistoryDao.Properties.Ic_card.like("%" + search_content + "%"),
                HistoryDao.Properties.Name.like("%" + search_content + "%"))
                .orderDesc(HistoryDao.Properties.Time);
        dataList = builder1.list();
        if (dataList.size() <= 20) {
            showDataList = dataList;
            showLastItem = dataList.size();
        } else {
            showDataList = dataList.subList(0, 20);
            showLastItem = 20;
        }
        handler.post(() -> {
//            initView();
            updateData();
            isPrepare = false;
        });
    }
}
