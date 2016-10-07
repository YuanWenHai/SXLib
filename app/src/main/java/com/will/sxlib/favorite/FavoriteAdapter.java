package com.will.sxlib.favorite;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.will.sxlib.R;
import com.will.sxlib.bean.FavoriteItem;
import com.will.sxlib.util.DBManager;

import java.util.List;

/**
 * Created by Will on 2016/10/3.
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyHolder>{
    private List<FavoriteItem> data;
    public FavoriteAdapter(){
        data = DBManager.getInstance().getAllItems();
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
        holder.state.setText(item.getState() == FavoriteItem.EXSIT ? "在馆" : "借出");
    }


    public void refreshData(){
        data.clear();
        data = DBManager.getInstance().getAllItems();
        notifyDataSetChanged();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        public TextView title,state;
        public MyHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.favorite_item_title);
            state = (TextView) view.findViewById(R.id.favorite_item_state);
        }
    }
}
