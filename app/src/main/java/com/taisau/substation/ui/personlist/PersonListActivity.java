package com.taisau.substation.ui.personlist;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;
import com.taisau.substation.SubstationApplication;
import com.taisau.substation.bean.Person;
import com.taisau.substation.bean.PersonDao;
import com.taisau.substation.ui.BaseActivity;
import com.taisau.substation.ui.personlist.adpter.PersonListAdapter;
import com.taisau.substation.util.ThreadPoolUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PersonListActivity extends BaseActivity {
    private PersonListAdapter adapter;
    private List<Person> dataList;
    private List<Person> showDataList;
    private LinearLayoutManager manager;
    private int lastVisibleItem;
    private static int showLastItem;
    private long lastInput;
    private long firstInput;
    private boolean isPrepare = false;
    private String search_content = "";
    private Handler handler = new Handler();
    private TextView tvDataTip;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);
        initView();
        initData();
    }

    public void initView() {
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        recyclerView = (RecyclerView) findViewById(R.id.personList_list);
        manager = new LinearLayoutManager(PersonListActivity.this);
        recyclerView.setLayoutManager(manager);
        tvDataTip = (TextView) findViewById(R.id.tv_tip);
        EditText search = (EditText) findViewById(R.id.personList_search_edit);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Logger.d("after change");
                lastInput = System.currentTimeMillis();
                search_content = s.toString();
                if (!isPrepare) {
                    Logger.d("start prepare");
                    isPrepare = true;
                    firstInput = System.currentTimeMillis();
                    ThreadPoolUtils.execute(searchCatcher);
                }
            }
        });
    }

    public void initData() {
        Observable.create(e -> {
            dataList = SubstationApplication.getApplication().getDaoSession().getPersonDao().loadAll();
            if (dataList.size() <= 20) {
                showDataList = dataList;
                showLastItem = dataList.size();
            } else {
                showDataList = dataList.subList(0, 20);
                showLastItem = 20;
            }
            e.onNext("");
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o ->
                        updateData()
                );
    }

    private void updateData() {
        if (showDataList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            tvDataTip.setVisibility(View.GONE);
            adapter = new PersonListAdapter(PersonListActivity.this, showDataList);
            recyclerView.setAdapter(adapter);
        } else {
            tvDataTip.setVisibility(View.VISIBLE);
            tvDataTip.setText(getResources().getString(R.string.no_data));
            recyclerView.setVisibility(View.GONE);
        }
        if (dataList.size() > 20) {
            recyclerView.addOnScrollListener(listener);
        }
    }


    public void doSearch(String search_content) {
        Observable.create(e -> {
            QueryBuilder<Person> builderB = SubstationApplication.getApplication().getDaoSession().getPersonDao().queryBuilder();
            builderB.where(PersonDao.Properties.Ic_card.like("%" + search_content + "%"));
            dataList = builderB.list();
            if (dataList.size() <= 20) {
                showDataList = dataList;
                showLastItem = dataList.size();
            } else {
                showDataList = dataList.subList(0, 20);
                showLastItem = 20;
            }
            e.onNext("");
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    updateData();
                    isPrepare = false;
                });
    }

    //滑动监听类
    RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, int newState) {
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
                    }
                    recyclerView.scrollToPosition(adapter.getItemCount() - 8);
                }, 1000);
            }
        }

        @Override
        public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
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
                }, 1000);
            }
        }
    };
    Runnable searchCatcher = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                try {
                    while (isPrepare) {
                        wait(2000);
                        if (lastInput - firstInput <= 1500) {
                            //do search
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
