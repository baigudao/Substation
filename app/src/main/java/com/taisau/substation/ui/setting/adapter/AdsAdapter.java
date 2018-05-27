package com.taisau.substation.ui.setting.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.taisau.substation.R;

import java.util.List;

/**
 * Created by Administrator on 2017/6/19 0019.
 */

public class AdsAdapter extends BaseAdapter {
    private int count;
    private List<String> imgPathList;
    private Context context;
    public AdsAdapter(List<String> imgPathList, Context context) {
        this.imgPathList=imgPathList;
        this.count=imgPathList.size();
        this.context=context;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getItem(int position) {
        return imgPathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdsHolder holder;
        if(null==convertView)
        {
            holder=new  AdsHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.content_ads_grid, null); //mContext指的是调用的Activtty
            holder.adsPreview=(ImageView) convertView.findViewById(R.id.ads_grid_img);
            convertView.setTag(holder);
        }
        else
        {
            holder=(AdsHolder) convertView.getTag();
        }
        holder.adsPreview.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/123test.jpg"));
        return convertView;
    }

    class AdsHolder{
        ImageView adsPreview;
    }
}
