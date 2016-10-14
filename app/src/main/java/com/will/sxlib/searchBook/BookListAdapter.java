package com.will.sxlib.searchBook;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.will.sxlib.R;
import com.will.sxlib.adapter.BaseRecyclerViewHolder;
import com.will.sxlib.adapter.CustomRecyclerAdapter;
import com.will.sxlib.base.MyApplication;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bookDetail.BookDetailActivity;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.NetworkHelper;

import java.util.List;

/**
 * Created by Will on 2016/10/1.
 */
public class BookListAdapter extends CustomRecyclerAdapter<Book> {
    private NetworkHelper helper = NetworkHelper.getInstance();
    private boolean hasMore = true;
    private Context mContext;

    private String keyword;
    private int sort;
    private int searchWay;

    public BookListAdapter(String keyword){
        this(keyword,NetworkHelper.SORT_BY_MATCHING,NetworkHelper.SEARCH_BY_DEFAULT);
    }
    public BookListAdapter(String keyword, int searchWay){
        this(keyword,searchWay,NetworkHelper.SORT_BY_MATCHING);
    }
    public BookListAdapter(String keyword, int searchWay, int sort){
        super(R.layout.book_item,R.layout.loading_view,R.layout.loading_failed_view);
        this.keyword = keyword;
        this.sort = sort;
        this.searchWay = searchWay;
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(Object item,BaseRecyclerViewHolder holder) {
                Book book = (Book) item;
                Intent intent = new Intent(mContext, BookDetailActivity.class);
                intent.putExtra("book",book);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext,holder.getConvertView(),"item").toBundle());
                }else{
                    mContext.startActivity(intent);
                }
            }
        });
    }
    @Override
    public void convert(final BaseRecyclerViewHolder holder, final Book item) {
        holder.setText(R.id.book_item_title,"书名："+item.getTitle())
                .setText(R.id.book_item_author,item.getAuthor())
                .setText(R.id.book_item_publish_year,item.getPublicationDate())
                .setText(R.id.book_item_publisher,item.getPublisher())
                .setText(R.id.book_item_isbn,"ISBN："+item.getIsbn());
        final ImageView cover = (ImageView) holder.getView(R.id.book_item_cover);
        if(mContext == null){
            mContext = getRecyclerView().getContext();
        }
        //因为图片地址是异步获取，将会产生延迟，故先将cover设置为loading图片，避免列表错乱
        Picasso.with(mContext).load(R.drawable.loading_image).into(cover);
        if(item.getCoverUrl() == null){
            helper.getBookCover(item.getIsbn().replaceAll("-", ""), new NetworkHelper.LoadCoverCallback() {
                @Override
                public void onResponse(String url) {
                    url = url == null ? "" : url;
                    item.setCoverUrl(url);
                    if(url.equals("")){
                        Picasso.with(mContext).load(R.drawable.no_image_available).into(cover);
                    }else {
                        Picasso.with(mContext).load(url).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(cover);
                    }
                }
            });
        }else {
            if (item.getCoverUrl().equals("")) {
                Picasso.with(mContext).load(R.drawable.no_image_available).into(cover);
            } else {
                Picasso.with(mContext).load(item.getCoverUrl()).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(cover);
            }
        }

    }

    @Override
    public void loadData(int page) {
        helper.keywordSearch(keyword, page,sort,searchWay, new NetworkHelper.KeySearchCallback() {
            @Override
            public void onResponse(List<Book> books, String count) {
                hasMore = books.size() == 10;
                if(!hasMore){
                    Toast.makeText(MyApplication.getGlobalContext(),"已到末页",Toast.LENGTH_SHORT).show();
                }
                update(true,books);
                setActivityTitle("共计"+count+"条");
            }

            @Override
            public void onFailure(ErrorCode code) {
                getRecyclerView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        update(false);
                    }
                },500);
            }
        });
    }

    @Override
    public boolean hasMoreData() {
        return hasMore;
    }
    private void setActivityTitle(String title){
        BookListActivity activity = (BookListActivity) getRecyclerView().getContext();
        activity.getSupportActionBar().setTitle(title);
    }


    public void refreshWithAnotherSort(int sort){
        this.sort = sort;
        refreshData();
    }
    public void refreshWithAnotherKeyword(String keyword){
        this.keyword = keyword;
        refreshData();
    }


}
