package com.taisau.substation.listener;


import com.taisau.substation.bean.Person;

/**
 * Created by Administrator on 2016/9/6 0006.
 */
public interface OnCardDetectListener {
    void onDetectCard(/*String icCardId*/Person person);
}
