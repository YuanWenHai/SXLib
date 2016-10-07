package com.will.sxlib.bookDetail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.will.sxlib.R;
import com.will.sxlib.bean.BookState;
import com.will.sxlib.bean.FavoriteItem;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/5/27.
 */
public class BookDetailAdapter extends RecyclerView.Adapter<BookDetailAdapter.BookDetailViewHolder> {
    private List<BookState> data;
    private Context context;
    private NetworkHelper helper;
    private LoadCallback callback;
    public BookDetailAdapter(Context context,String bookNumber){
        data = new ArrayList<>();
        helper = NetworkHelper.getInstance();
        this.context = context;
        loadState(bookNumber);
    }
    private void loadState(String bookNumber){
        helper.getBookState(bookNumber, new NetworkHelper.LoadStateCallback() {
            @Override
            public void onResponse(List<BookState> bookState) {
                data.addAll(bookState);
                notifyItemRangeChanged(0,bookState.size());
                if(bookState.size() == 0){
                    Toast.makeText(context,"无馆藏信息",Toast.LENGTH_SHORT).show();
                }
                if(callback != null){
                    callback.onSuccess();
                }
            }
            @Override
            public void onFailure(ErrorCode code) {
                Toast.makeText(context, "网络连接失败", Toast.LENGTH_SHORT).show();
                if(callback != null){
                    callback.onFailure(code);
                }
            }
        });
    }
    public int getItemCount(){
        return data.size();
    }
    public BookDetailViewHolder onCreateViewHolder(ViewGroup parent,int type){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_item,parent,false);
        return new BookDetailViewHolder(view);
    }
    public void onBindViewHolder(BookDetailViewHolder holder,int position){
        BookState state = data.get(position);
        holder.position.setText(state.getCurlocal());
        holder.barCode.setText(state.getBarcode());
        holder.callno.setText(state.getCallno());
        holder.state.setText(state.getState());
        holder.lib.setText(state.getCurlib());
        if(state.getLoanData() != null){
            holder.returnDate.setText(state.getLoanData().getReturnDateStr());
        }else{
            holder.returnDate.setText("-");
        }
    }
    public void setLoadCallback(LoadCallback callback){
        this.callback = callback;
    }




    class BookDetailViewHolder extends RecyclerView.ViewHolder{
        public TextView callno,barCode,state,returnDate,lib,position;
        public BookDetailViewHolder(View v){
            super(v);
            callno = (TextView) v.findViewById(R.id.book_detail_table_call_no);
            barCode = (TextView) v.findViewById(R.id.book_detail_table_bar_code);
            state = (TextView) v.findViewById(R.id.book_detail_table_state);
            returnDate  = (TextView) v.findViewById(R.id.book_detail_table_return_date);
            lib = (TextView) v.findViewById(R.id.book_detail_table_lib);
            position = (TextView) v.findViewById(R.id.book_detail_table_position);
        }
    }
    public List<BookState> getBookStateData(){
        return data;
    }

    /**
     * 获取此书馆藏状态，只要有一条项目存在，即为存在，反之为不存在
     * @return state
     */
    public int getBookState(){
        for(BookState state :getBookStateData()){
            if(state.getState().equals("在馆")){
                return FavoriteItem.EXSIT;
            }
        }
        return FavoriteItem.NON_EXSIT;
    }
    interface LoadCallback{
        void onSuccess();
        void onFailure(ErrorCode code);
    }
}
