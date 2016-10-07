package com.will.sxlib.myBook;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.will.sxlib.MainActivity;
import com.will.sxlib.R;
import com.will.sxlib.base.BaseActivity;
import com.will.sxlib.base.BaseFragment;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.SPHelper;

/**
 * Created by Will on 2016/5/31.
 */
public class MyBookFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout refreshLayout;
    private MyBookAdapter adapter;
    private Switch switcher;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view  = inflater.inflate(R.layout.fragment_my_book,null);
        switcher = (Switch)view.findViewById(R.id.my_book_switcher);
        switcher.setChecked(SPHelper.getReturnNotificationState());
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPHelper.setReturnNotificationState(isChecked);
            }
        });
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.my_book_refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.my_book_recycler_view);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.my_book_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).changeDrawerState();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyBookAdapter( getActivity());
        adapter.setCallbacks(new MyBookAdapter.RenewCallback() {
            @Override
            public void onSuccess() {
                showToast("续借成功");
            }
            @Override
            public void onFailure(ErrorCode code) {
                if(code == ErrorCode.CONNECTION_FAILED){
                    showToast("网络连接失败");
                }else if (code == ErrorCode.PASSWORD_INVALID){
                    showToast("密码错误，请重新登陆!");
                } else if (code == ErrorCode.RENEW_FAILED){
                    showToast("续借已达最大次数！");
                }
            }
        }, new MyBookAdapter.LoadCallback() {
            @Override
            public void onSuccess(int count) {
                if(count == 0){
                    ((BaseActivity) getActivity()).getSupportActionBar().setTitle("无借阅数据");
                }else{
                    ((BaseActivity) getActivity()).getSupportActionBar().setTitle("我的借阅");
                }
                refreshLayout.setRefreshing(false);
            }
            @Override
            public void onFailure(ErrorCode code) {
               if(code == ErrorCode.CONNECTION_FAILED){
                   ((BaseActivity) getActivity()).getSupportActionBar().setTitle("加载失败");
               }else if (code == ErrorCode.PASSWORD_INVALID){
                   showToast("密码已被修改，请重新登录");
                   ((MainActivity)getActivity()).logout();
                   ((MainActivity)getActivity()).switchNavigationItems(MainActivity.SEARCH);
               }
                refreshLayout.setRefreshing(false);
            }
        });
        recyclerView.setAdapter(adapter);
        ((BaseActivity) getActivity()).getSupportActionBar().setTitle("载入中···");
        return view;
    }
    @Override
    public void onRefresh(){
        adapter.loadData();
    }

}
