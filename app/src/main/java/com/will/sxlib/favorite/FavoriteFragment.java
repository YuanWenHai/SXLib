package com.will.sxlib.favorite;

import android.content.Intent;
import android.os.Bundle;
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
import com.will.sxlib.base.BaseFragment;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bookDetail.BookDetailActivity;
import com.will.sxlib.util.SPHelper;

/**
 * Created by Will on 2016/10/3.
 */
public class FavoriteFragment extends BaseFragment {
    private FavoriteAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite,container,false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.favorite_recycler_view);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.favorite_toolbar);
        setupToolbar(toolbar);
        Switch switcher = (Switch)view.findViewById(R.id.favorite_switcher);
        setupSwitcher(switcher);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FavoriteAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FavoriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra("book",book);
                getActivity().startActivity(intent);
            }
        });
        return view;
    }

    private void setupSwitcher(Switch switcher){
        switcher.setChecked(SPHelper.getLoanableNotificationState());
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPHelper.setLoanableNotificationState(isChecked);
            }
        });
    }



    private void setupToolbar(Toolbar toolbar){
        final MainActivity activity = ((MainActivity)getActivity());
        activity.setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeDrawerState();
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            adapter.refreshData();
        }
    }
}
