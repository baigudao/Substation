package com.taisau.substation.ui.history.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.taisau.substation.R;
import com.taisau.substation.bean.History;
import com.taisau.substation.ui.history.PrintHistoryActivity;
import com.taisau.substation.widget.GlideCircleTransform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by whx on 2017/11/06
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOOTER = 1, TYPE_ITEM = 0, TYPE_NOMORE = 2;
    private static int load_more_status = 0;
    public Context context;
    private List list = new ArrayList();
    private RequestOptions requestOptions = new RequestOptions().centerCrop().transform(
            new GlideCircleTransform(context)).dontAnimate().placeholder(R.drawable.bg_oval_gray)
            .error(R.drawable.bg_oval_gray).diskCacheStrategy(DiskCacheStrategy.ALL);

    public HistoryAdapter(Context context, List<History> list) {
        load_more_status = 0;
        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //进行判断显示类型，来创建返回不同的View
        if (viewType == TYPE_ITEM) {

            View view = LayoutInflater.from(context).inflate(R.layout.content_history_list_layout, parent, false);
            //这边可以做一些属性设置，甚至事件监听绑定
            //view.setBackgroundColor(Color.RED);

            return new HistoryView(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.recyle_load_more, parent, false);
            //这边可以做一些属性设置，甚至事件监听绑定
            //view.setBackgroundColor(Color.RED);
            return new LoadMore(view);
        } else if (viewType == TYPE_NOMORE) {
            View view = LayoutInflater.from(context).inflate(R.layout.recyle_load_nomore, parent, false);
            //这边可以做一些属性设置，甚至事件监听绑定
            //view.setBackgroundColor(Color.RED);
            return new NoMore(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HistoryView) {
            final History person = (History) list.get(position);
            HistoryView historyView = (HistoryView) holder;
            String ic_card = context.getResources().getString(R.string.ic_card_num);
            String ic_card2 = String.format(ic_card, person.getIc_card());
            historyView.ic_card.setText(ic_card2);
            historyView.result.setText(person.getCom_status());
            String time = context.getResources().getString(R.string.time_instance);
            String time2 = String.format(time, person.getTime());
            historyView.time.setText(time2);
            Glide.with(context).load(new File(person.getFace_path())).apply(requestOptions).into(historyView.avatar);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PrintHistoryActivity.class);
                intent.putExtra("his_id", person.getId());
                context.startActivity(intent);
            });
//            if (person.getUpload_status()) {
//                historyView.tvUploadStatus.setText("上传成功");
//                historyView.tvUploadStatus.setTextColor(context.getResources().getColor(R.color.color_0dd63c));
//            } else {
//                historyView.tvUploadStatus.setText("上传失败");
//                historyView.tvUploadStatus.setTextColor(context.getResources().getColor(R.color.color_f73030));
//            }
        }
        if (holder instanceof LoadMore) {
            LoadMore loadMore = (LoadMore) holder;
            switch (load_more_status) {
                case 0:
                    loadMore.itemView.setVisibility(View.GONE);
                    break;
                case 1:
                    loadMore.itemView.setVisibility(View.VISIBLE);
                    break;
            }
        }
        if (holder instanceof NoMore) {
            NoMore noMore = (NoMore) holder;
        }
    }


    @Override
    public int getItemCount() {

        if (list.size() >= 20) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount() && list.size() >= 20) {
            if (load_more_status == 3)
                return TYPE_NOMORE;
            else
                return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public class HistoryView extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView ic_card;
        TextView result;
        TextView time;
        TextView tvUploadStatus;

        public HistoryView(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.history_list_avatar);
            ic_card = (TextView) itemView.findViewById(R.id.history_list_card_id);
            result = (TextView) itemView.findViewById(R.id.history_list_result);
            time = (TextView) itemView.findViewById(R.id.history_list_time);
            tvUploadStatus = (TextView) itemView.findViewById(R.id.tv_upload_status);
        }
    }

    public class LoadMore extends RecyclerView.ViewHolder {
        public LoadMore(View itemView) {
            super(itemView);
        }
    }

    public class NoMore extends RecyclerView.ViewHolder {
        public NoMore(View itemView) {
            super(itemView);
        }
    }

    public void changeMoreStatus(int status, List list) {
        if (list != null)
            this.list = list;
        load_more_status = status;
        notifyDataSetChanged();
    }
}
