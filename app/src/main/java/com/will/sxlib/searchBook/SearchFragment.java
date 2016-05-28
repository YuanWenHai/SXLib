package com.will.sxlib.searchBook;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.will.sxlib.R;
import com.will.sxlib.base.BaseFragment;

/**
 * Created by Will on 2016/5/25.
 */
public class SearchFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search,null);
        final EditText editText = (EditText) view.findViewById(R.id.search_edit_text);
        ImageButton button = (ImageButton) view.findViewById(R.id.search_commit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString();
                if(!content.isEmpty()){
                    Intent intent = new Intent(getActivity(),BookListActivity.class);
                    intent.putExtra("keyword",content);
                    startActivity(intent);
                }else{
                    showToast("检索词为空!");
                }
            }
        });
        return view;
    }
}
