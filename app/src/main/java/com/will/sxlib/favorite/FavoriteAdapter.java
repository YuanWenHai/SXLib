package com.will.sxlib.favorite;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.will.sxlib.R;
import com.will.sxlib.base.MyApplication;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bean.FavoriteItem;
import com.will.sxlib.util.DBManager;
import com.will.sxlib.view.RemovableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/10/3.
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyHolder> {
    private List<FavoriteItem> data;
    private Context mContext;
    private OnItemClickListener listener;
    private RecyclerView mRecyclerView;
    private int lastAnimatedIndex = -1;
    public FavoriteAdapter(){
        data = DBManager.getInstance().getAllItems();
    }
    public FavoriteAdapter(Context context){
        this();
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        FavoriteItem item = data.get(position);
        holder.title.setText("书名："+item.getTitle());
        holder.author.setText(item.getAuthor());
        holder.publishDate.setText(item.getPublicationDate());
        holder.press.setText(item.getPublisher());
        holder.isbn.setText("ISBN："+item.getIsbn());

        holder.state.setText( item.getState() == FavoriteItem.EXSIT ?
                "在馆"  : "借出");
        if(item.getState() == FavoriteItem.EXSIT ){
            holder.state.setText("在馆");
            holder.state.setTextColor(MyApplication.getGlobalContext().getResources().getColor(R.color.colorPrimary));
            holder.state.setBackgroundResource(R.drawable.border_indigo);
        }else{
            holder.state.setText("借出");
            holder.state.setTextColor(MyApplication.getGlobalContext().getResources().getColor(R.color.material_red));
            holder.state.setBackgroundResource(R.drawable.border_red);
        }
        Picasso.with(mContext).load(R.drawable.loading_image).into(holder.cover);
        String url = item.getCoverUrl();
        if(url == null ||url.isEmpty()){
            Picasso.with(mContext).load(R.drawable.no_image_available).into(holder.cover);
        }else{
            Picasso.with(mContext).load(url).into(holder.cover);
        }
        animateView(holder.itemView,position);
    }


    public void refreshData(){
        data.clear();
        lastAnimatedIndex = -1;
        data = DBManager.getInstance().getAllItems();
        notifyDataSetChanged();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        public TextView title,author, publishDate,press,isbn,state;
        public ImageView cover;
        public MyHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.book_item_title);
            state = (TextView) view.findViewById(R.id.favorite_item_state);
            author = (TextView) view.findViewById(R.id.book_item_author);
            publishDate = (TextView) view.findViewById(R.id.book_item_publish_year);
            press = (TextView) view.findViewById(R.id.book_item_publisher);
            isbn = (TextView) view.findViewById(R.id.book_item_isbn);
            cover = (ImageView) view.findViewById(R.id.book_item_cover);
            //这里有点邪门，在使用include引入其他layout后对view设置onClick事件竟失效了，故重新findView
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        listener.onItemClick(data.get(getAdapterPosition()));
                    }
                }
            });
            ((RemovableView)view).setOnRemoveCallback(new RemovableView.OnRemoveCallback() {
                @Override
                public void onRemove(View removedView) {
                    removeItem(getAdapterPosition());
                }
            });
            ((RemovableView)view).setAutoRemoveMultiplier(0.3f);
        }
    }

    void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    interface OnItemClickListener {
        void onItemClick(Book book);
    }
    private void animateView(final View view, final int position){
       if(lastAnimatedIndex < position){
           final TranslateAnimation translateAnimation = new TranslateAnimation(mRecyclerView.getWidth(),mRecyclerView.getX(),0,0);
           translateAnimation.setDuration(300);
           view.setVisibility(View.INVISIBLE);
           mRecyclerView.postDelayed(new Runnable() {
               @Override
               public void run() {
                   view.startAnimation(translateAnimation);
                   view.setVisibility(View.VISIBLE);
                   lastAnimatedIndex = position;
               }
           },200);
       }
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if(mRecyclerView == null){
            this.mRecyclerView = recyclerView;
        }
    }
    private List<FavoriteItem> getTestData(){
        ArrayList<FavoriteItem> list = new ArrayList<>();
        for(int i = 0; i<100;i++){
            list.add(new FavoriteItem());
        }
        return list;
    }
    private void removeItem(int position){
        DBManager.getInstance().deleteItemByTitle(data.get(position).getTitle());
        data.remove(position);
        lastAnimatedIndex = position -1;
        notifyDataSetChanged();
    }
}
