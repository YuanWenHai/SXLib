package com.will.sxlib.bookDetail;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.will.sxlib.R;
import com.will.sxlib.base.BaseActivity;
import com.will.sxlib.bean.Book;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.NetworkHelper;

import java.util.Map;

/**
 * Created by Will on 2016/5/27.
 */
public class BookDetailActivity extends BaseActivity {
    private Book book;
    private LinearLayout root;
    private LinearLayout bookInfo;
    private RelativeLayout gcxxTable;
    private RecyclerView recyclerView;
    private TextView title,language,edition,page,theme,summary;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_book_detail);
        book = (Book) getIntent().getSerializableExtra("book");
        initializeView();
        NetworkHelper helper = NetworkHelper.getInstance();
        helper.loadBookDetail(book.getBookNumber(), new NetworkHelper.LoadDetailCallback() {
            @Override
            public void onResponse(String titleStr, Map<String, String> map) {
                title.setText(titleStr);
                language.setText("语言： "+map.get("语种:"));
                edition.setText("版次： "+map.get("版次:"));
                page.setText("载体形态： "+map.get("载体形态:"));
                theme.setText("主题： "+map.get("主题:"));
                summary.setText("摘要： "+map.get("摘要:"));
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    TransitionManager.beginDelayedTransition(root,new Slide().setDuration(500));
                }else{
                    TransitionManager.beginDelayedTransition(root,new Fade().setDuration(500));
                }
                bookInfo.setVisibility(View.VISIBLE);
                //
                BookDetailAdapter adapter = new BookDetailAdapter(BookDetailActivity.this,book.getBookNumber());
                adapter.setLoadCallback(new BookDetailAdapter.LoadCallback() {
                    @Override
                    public void onSuccess() {
                        getSupportActionBar().setTitle("详情");
                    }

                    @Override
                    public void onFailure(ErrorCode code) {
                        getSupportActionBar().setTitle("载入组件失败");
                    }
                });
                recyclerView.setAdapter(adapter);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    TransitionManager.beginDelayedTransition(root,new Slide().setDuration(500));
                }else{
                    TransitionManager.beginDelayedTransition(root,new Fade().setDuration(500));
                }
                gcxxTable.setVisibility(View.VISIBLE);
            }
            @Override
            public void onFailure(ErrorCode code) {
                showToast("获取信息失败");
            }
        });
    }
    private void initializeView(){
        title = (TextView) findViewById(R.id.book_detail_title);
        language = (TextView) findViewById(R.id.book_detail_language);
        edition = (TextView) findViewById(R.id.book_detail_edition);
        page = (TextView) findViewById(R.id.book_detail_page_count);
        theme = (TextView) findViewById(R.id.book_detail_theme);
        summary = (TextView) findViewById(R.id.book_detail_summary);
        bookInfo = (LinearLayout) findViewById(R.id.book_detail_layout);
        gcxxTable = (RelativeLayout) findViewById(R.id.book_detail_gcxx_layout);
        root = (LinearLayout) findViewById(R.id.book_detail_root);
        recyclerView = (RecyclerView) findViewById(R.id.book_detail_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.book_detail_toolbar);
        toolbar.setTitle("加载中···");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //
        TextView title = (TextView) findViewById(R.id.book_item_title);
        TextView author = (TextView) findViewById(R.id.book_item_author);
        TextView publicationDate = (TextView) findViewById(R.id.book_item_publish_year);
        TextView publisher = (TextView) findViewById(R.id.book_item_publisher);
        TextView isbn = (TextView) findViewById(R.id.book_item_isbn);
        ImageView cover = (ImageView) findViewById(R.id.book_item_cover);
        title.setText("书名："+book.getTitle());
        author.setText(book.getAuthor());
        publicationDate.setText(book.getPublicationDate());
        publisher.setText(book.getPublisher());
        isbn.setText("ISBN："+book.getIsbn());
        if (book.getCoverUrl() == null || book.getCoverUrl().equals("")) {
            Picasso.with(this).load(R.drawable.no_image_available).into(cover);
        } else {
            Picasso.with(this).load(book.getCoverUrl()).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(cover);
        }
    }
}
