package com.will.sxlib.myBook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.will.sxlib.R;
import com.will.sxlib.bean.MyBook;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.UserOperationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/5/31.
 */
public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.MyBookViewHolder> {
    private String account;
    private String password;
    private Context context;
    private List<MyBook> data = new ArrayList<>();
    private UserOperationHelper helper;
    private RenewCallback renewCallback;
    private LoadCallback loadCallback;
    public MyBookAdapter(Context context){
        this.context = context;
        getAccountInfo();
        helper = UserOperationHelper.getInstance(context,account,password);
        loadData();
    }

    public void loadData(){
        helper.getLoanData(new UserOperationHelper.RenewCallback() {
            @Override
            public void onResponse(List<MyBook> list) {
                data.clear();
                data.addAll(list);
                notifyItemRangeChanged(0,list.size());
                if(loadCallback != null){
                    loadCallback.onSuccess(data.size());
                }
            }
            @Override
            public void onFailure(ErrorCode code) {
                if(loadCallback != null){
                    loadCallback.onFailure(code);
                }else {
                    Toast.makeText(context,"网络连接失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public int getItemCount(){
        return data.size();
    }
    @Override
    public MyBookViewHolder onCreateViewHolder(ViewGroup parent,int type){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_book_item,parent,false);
        return new MyBookViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyBookViewHolder holder,int position){
        MyBook book = data.get(position);
        holder.barCode.setText(book.getBarCode());
        holder.title.setText(book.getTitle());
        holder.callNo.setText(book.getCallNo());
        holder.local.setText(book.getLocal());
        holder.type.setText(book.getType());
        holder.volumeInfo.setText(book.getVolumeInfo());
        holder.loanDate.setText(book.getLoanDate());
        holder.returnDate.setText(book.getReturnDate());
        holder.renewCount.setText(book.getRenewCount());
    }
    class MyBookViewHolder extends RecyclerView.ViewHolder{
        public TextView barCode,title,callNo,local,type,volumeInfo,loanDate,returnDate,renewCount;
        private ImageButton button;
        public MyBookViewHolder(View v){
            super(v);
            barCode = (TextView)v.findViewById(R.id.my_book_bar_code);
            title = (TextView) v.findViewById(R.id.my_book_title);
            callNo = (TextView) v.findViewById(R.id.my_book_call_no);
            local = (TextView) v.findViewById(R.id.my_book_local);
            type = (TextView) v.findViewById(R.id.my_book_type);
            volumeInfo =(TextView) v.findViewById(R.id.my_book_volume_info);
            loanDate = (TextView) v.findViewById(R.id.my_book_loan_date);
            returnDate = (TextView) v.findViewById(R.id.my_book_return_date);
            renewCount = (TextView) v.findViewById(R.id.my_book_renew_count);
            button = (ImageButton) v.findViewById(R.id.my_book_button_renew);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(data.get(getAdapterPosition()).getTitle().replace("题名:",""));
                    builder.setMessage("续借成功后可延期一个月，一本书最多可续借一次,确定续借吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(renewCallback != null){
                                helper.renewBook(data.get(getAdapterPosition()).getBarCode().replace("条码号: ",""), new UserOperationHelper.DoRenewCallback() {
                                    @Override
                                    public void onSuccess() {
                                        loadData();
                                        renewCallback.onSuccess();
                                    }
                                    @Override
                                    public void onFailure(ErrorCode code) {
                                        renewCallback.onFailure(code);
                                    }
                                });
                            }
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.create().show();

                }
            });
        }
    }
    private void getAccountInfo(){
        SharedPreferences sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        account = sp.getString("account","");
        password = sp.getString("password","");
    }

    /**
     * 设置获取借阅信息以及续借键点击事件的callback
     * @param renewCallback 点击续借callback
     * @param loadCallback 加载借阅信息callback
     */
    public void setCallbacks(RenewCallback renewCallback,LoadCallback loadCallback){
        this.renewCallback = renewCallback;
        this.loadCallback = loadCallback;
    }
    public interface RenewCallback{
        void onSuccess();
        void onFailure(ErrorCode code);
    }
    public interface LoadCallback{
        void onSuccess(int count);
        void onFailure(ErrorCode code);
    }
}
