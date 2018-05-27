package com.taisau.substation.ui.personlist.adpter;

import android.content.Context;
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
import com.taisau.substation.bean.Person;
import com.taisau.substation.widget.GlideCircleTransform;

import java.util.List;

/**
 * Created by Administrator on 2017/4/17 0017
 */

public class PersonListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOOTER = 1, TYPE_ITEM = 0, TYPE_NOMORE = 2;
    private static int load_more_status = 0;
    public Context context;
    private List list;
    private RequestOptions requestOptions = new RequestOptions().centerCrop().transform(new GlideCircleTransform(context)).dontAnimate().placeholder(R.drawable.bg_oval_gray).error(R.drawable.bg_oval_gray)
            .diskCacheStrategy(DiskCacheStrategy.ALL);
    public PersonListAdapter(Context context, List<Person> list) {

        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //进行判断显示类型，来创建返回不同的View
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.content_person_list_layout, parent, false);
            //这边可以做一些属性设置，甚至事件监听绑定
            //view.setBackgroundColor(Color.RED);
            return new PersonInfoView(view);
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
        if (holder instanceof PersonInfoView) {
            final Person person = (Person) list.get(position);
            PersonInfoView infoView = (PersonInfoView) holder;
            String ic_card = context.getResources().getString(R.string.ic_card_num);
            String ic_card2 = String.format(ic_card,person.getIc_card());
            ((PersonInfoView) holder).name.setText(ic_card2);
            String imgUrl = person.getImg_path();
//            List<FaceList> fl = FaceIcApplication.getApplication().getDaoSession().getFaceListDao().queryBuilder().where(FaceListDao.Properties.PersonId.eq(person.getPerson_id())).list();
//            if (fl.size() > 0)
//                imgUrl = fl.get(0).getImg_url();
//            if (imgUrl != null && !imgUrl.equals(""))
                Glide.with(context).load(imgUrl).apply(requestOptions).into(infoView.avatar);
//            else
//                infoView.avatar.setVisibility(View.INVISIBLE);
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

        if (position+1== getItemCount()&&list.size()>=20) {
            if (load_more_status==3)
                return TYPE_NOMORE;
            else
                return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public class PersonInfoView extends RecyclerView.ViewHolder {
        TextView name;
        ImageView avatar;

        public PersonInfoView(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.person_list_name_id);
            avatar = (ImageView) itemView.findViewById(R.id.person_list_avatar);
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
