package com.will.sxlib.favorite;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.will.sxlib.R;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bean.FavoriteItem;
import com.will.sxlib.util.DBManager;

import java.util.List;

/**
 * Created by Will on 2016/10/3.
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyHolder>{
    private List<FavoriteItem> data;
    private Context mContext;
    private OnItemClickListener listener;
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
        holder.title.setText(item.getTitle());
        holder.author.setText(item.getAuthor());
        holder.publishDate.setText(item.getPublicationDate());
        holder.press.setText(item.getPublisher());
        holder.isbn.setText(item.getIsbn());

        holder.state.setText( item.getState() == FavoriteItem.EXSIT ?
                "在馆"  : "借出");
        Picasso.with(mContext).load(R.drawable.loading_image).into(holder.cover);
        String url = item.getCoverUrl();
        if(url.isEmpty()){
            holder.cover.setImageResource(R.drawable.no_image_available);
        }else{
            Picasso.with(mContext).load(url).into(holder.cover);
        }
    }


    public void refreshData(){
        data.clear();
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
            view.findViewById(R.id.favorite_item_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        listener.onItemClick(data.get(getAdapterPosition()));
                    }
                }
            });
        }
    }

    void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    interface OnItemClickListener {
        void onItemClick(Book book);
    }
}
