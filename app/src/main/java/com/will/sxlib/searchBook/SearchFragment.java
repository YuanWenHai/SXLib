package com.will.sxlib.searchBook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.view.BodyTextView;
import com.arlib.floatingsearchview.util.view.IconImageView;
import com.will.sxlib.MainActivity;
import com.will.sxlib.R;
import com.will.sxlib.base.BaseFragment;
import com.will.sxlib.searchBook.bean.SearchTypeSuggestion;
import com.will.sxlib.util.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/5/28.
 */
public class SearchFragment extends BaseFragment {
    private String query = "";//这里因为这个searchview有点问题，无法获取到实时的query，所以干脆在onQueryChanged里每次都获取query
    public View onCreateView(final LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view  = inflater.inflate(R.layout.fragment_search,null);
        final FloatingSearchView searchView = (FloatingSearchView) view.findViewById(R.id.search_fragment_search_view);
        ImageView menu = (ImageView) view.findViewById(R.id.search_fragment_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeDrawerState();
            }
        });
        searchView.setSuggestionRightIconColor(Color.TRANSPARENT);
        //在搜索栏得到焦点后，将statusBar的颜色设置为暗色
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                ((MainActivity)getActivity()).getStatusBar().setBackgroundColor(Color.parseColor("#aa000000"));
            }
            @Override
            public void onFocusCleared() {
                ((MainActivity)getActivity()).getStatusBar().setBackgroundResource(R.drawable.status_bar_bg);
            }
        });
        searchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(IconImageView leftIcon, BodyTextView bodyText, SearchSuggestion item, int itemPosition) {
                leftIcon.setImageResource(R.drawable.ic_search_black_24dp);
                if(itemPosition == 0){
                    bodyText.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    bodyText.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                List<SearchTypeSuggestion> suggestions = new ArrayList<>();
                suggestions.add(new SearchTypeSuggestion("搜索书名:   "+newQuery));
                suggestions.add(new SearchTypeSuggestion("搜索作者:   "+newQuery));
                query = newQuery;
                searchView.swapSuggestions(suggestions);
            }
        });
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                if(!query.isEmpty()){
                    Intent intent = new Intent(getActivity(),BookListActivity.class);
                    intent.putExtra("query",query);
                    if(searchSuggestion.getBody().contains("书名")){
                        intent.putExtra("mode", NetworkHelper.SEARCH_BY_TITLE);
                    }else{
                        intent.putExtra("mode",NetworkHelper.SEARCH_BY_AUTHOR);
                    }
                    startActivity(intent);
                }else{
                    showToast("检索词为空！");
                }
            }

            @Override
            public void onSearchAction() {
                if (!query.isEmpty()){
                    Intent intent = new Intent(getActivity(),BookListActivity.class);
                    intent.putExtra("query",query);
                    intent.putExtra("mode", NetworkHelper.SEARCH_BY_DEFAULT);
                    startActivity(intent);
                }else{
                    showToast("检索词为空！");
                }
            }
        });
        return view;
    }
}
