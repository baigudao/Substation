package com.taisau.substation.ui.setting.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.taisau.substation.R;

import java.util.ArrayList;

/**
 * Created by whx on 2017/8/16
 */

public class NetworkSettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "NetworkSettingAdapter";
    private Context context;
    private ArrayList<String> networkList;
    private SparseArray<String> valueList;

    public NetworkSettingAdapter(Context context, ArrayList<String> list, SparseArray<String> values) {
        this.context = context;
        this.networkList = list;
        this.valueList = values;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_setting_network_item, parent, false);
        view.setOnClickListener(this);
        return new NetHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NetHolder netHolder = (NetHolder) holder;
        netHolder.title.setText(networkList.get(position));
        holder.itemView.setTag(position);
        if(position==1 || position==3){
            netHolder.tvAddress2.setVisibility(View.INVISIBLE);
            netHolder.tvAddress3.setVisibility(View.INVISIBLE);
            netHolder.tvAddress4.setVisibility(View.INVISIBLE);
        }else{
            netHolder.tvAddress2.setVisibility(View.VISIBLE);
            netHolder.tvAddress3.setVisibility(View.VISIBLE);
            netHolder.tvAddress4.setVisibility(View.VISIBLE);
        }
        try {
            String addr = valueList.get(position);
            if (addr == null) {
                netHolder.tvAddress1.setText("");
                netHolder.tvAddress2.setText("");
                netHolder.tvAddress3.setText("");
                netHolder.tvAddress4.setText("");
            } else {
                Logger.d( "onBindViewHolder:addr=" + addr);
                if(position==1 || position==3){
                    netHolder.tvAddress1.setText(addr);
                }else{
                    String[] address = addr.split("\\.");
                    netHolder.tvAddress1.setText(address[0]);
                    netHolder.tvAddress2.setText(address[1]);
                    netHolder.tvAddress3.setText(address[2]);
                    netHolder.tvAddress4.setText(address[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return networkList.size();
    }

    private class NetHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView tvAddress1, tvAddress2, tvAddress3, tvAddress4;
        RelativeLayout rl_address;

        NetHolder(View itemView) {
            super(itemView);
            rl_address = (RelativeLayout) itemView.findViewById(R.id.rl_address);
            title = (TextView) itemView.findViewById(R.id.tv_network_item_name);
            tvAddress1 = (TextView) itemView.findViewById(R.id.et_address1);
            tvAddress2 = (TextView) itemView.findViewById(R.id.et_address2);
            tvAddress3 = (TextView) itemView.findViewById(R.id.et_address3);
            tvAddress4 = (TextView) itemView.findViewById(R.id.et_address4);

        }
    }

    private OnItemClickListener mOnItemClickListener = null;

    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void updateAddress(int position, String address) {
        valueList.setValueAt(position,address);
    }
}
