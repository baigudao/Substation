package com.taisau.substation.ui.setting.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.taisau.substation.R;

/**
 * Created by Administrator on 2017/8/10 0010.
 */

public class SettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;

    public SettingAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.content_setting_list_item,parent,false);
        //这边可以做一些属性设置，甚至事件监听绑定
        //view.setBackgroundColor(Color.RED);
        SettingView settingView=new SettingView(view);
        return settingView;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SettingView view=(SettingView)holder;
        if (position<4)
        {
            view.button.setVisibility(View.GONE);
            view.ver1.setVisibility(View.GONE);
            view.ver2.setVisibility(View.GONE);
            view.ver3.setVisibility(View.GONE);
            switch (position){
                case 0:
                    view.setting.setText("显示设置");
            }
        }
        else if (position==4)
        {
            view.setting.setVisibility(View.GONE);
            view.button.setVisibility(View.GONE);


        }
        else if(position==5)
        {
            view.setting.setVisibility(View.GONE);
            view.divide.setVisibility(View.GONE);

            view.ver1.setVisibility(View.GONE);
            view.ver2.setVisibility(View.GONE);
            view.ver3.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return 6;
    }

    public class SettingView extends RecyclerView.ViewHolder {
        TextView setting,ver1,ver3;
        ImageView ver2;
        Button button;
        View divide;
        public SettingView(View itemView) {
            super(itemView);
            setting=(TextView)itemView.findViewById(R.id.setting_list_item_text);
            button=(Button)itemView.findViewById(R.id.setting_list_item_Button);
            divide=itemView.findViewById(R.id.setting_list_divide);

            ver1=(TextView)itemView.findViewById(R.id.setting_list_item_version1);
            ver3=(TextView)itemView.findViewById(R.id.setting_list_item_version3);

            ver2=(ImageView)itemView.findViewById(R.id.setting_list_item_version2);
        }
    }
}
