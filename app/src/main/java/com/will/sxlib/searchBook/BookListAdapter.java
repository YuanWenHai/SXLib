package com.will.sxlib.searchBook;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.will.sxlib.R;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bookDetail.BookDetailActivity;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/5/26.
 */
public class BookListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_BOOK = 0;
    private static final int ITEM_LOADING = 1;
    private List<Book> data;
    private Context context;
    private String total = "0";
    private NetworkHelper helper;
    private String keyword = "";
    private int page = 0;
    private boolean isEnd;
    private int sort;
    private int searchWay;
    public BookListAdapter (Context context){
        this.context = context;
        data = new ArrayList<>();
        helper = NetworkHelper.getInstance(context);
    }

    /**
     * 获取总条目数，本方法必须在调用search且callback返回后调用！
     * @return 总条目数
     */
    public String getBookCount(){
        return total;
    }

    /**
     * 关键词搜索，本方法只做搜索使用，也就是说，任何时候返回的都是都是第一页内容
     * @param keyword 关键词
     * @param callback callback
     */
    public void search(String keyword, final int sort, int searchWay,final LoadDataCallback callback){
        this.keyword = keyword;
        this.sort = sort;
        this.searchWay = searchWay;
        helper.keywordSearch(keyword, 1,sort,searchWay, new NetworkHelper.KeySearchCallback() {
            @Override
            public void onResponse(List<Book> books, String count) {
                data.clear();
                data.addAll(books);
                total = count;
                page = 1;
                isEnd = books.size() < 10;
                //notifyItemRangeChanged(0,data.size());
                notifyDataSetChanged();
                callback.onSuccess();
            }

            @Override
            public void onFailure(ErrorCode code) {
                callback.onFailure(code);
            }
        });
    }

    /**
     * 加载更多，此方法必须在调用search方法之后再使用，否则无keyword数据。
     */
    public void loadMore(){
        helper.keywordSearch(keyword, page+1, sort,searchWay,new NetworkHelper.KeySearchCallback() {
            @Override
            public void onResponse(List<Book> books, String total) {
                page++;
                int index = data.size();
                data.addAll(books);
                if(books.size() < 10){
                    isEnd = true;
                    Toast.makeText(context,"已无更多",Toast.LENGTH_SHORT).show();
                }
                notifyItemRangeChanged(index-1,data.size());

            }
            @Override
            public void onFailure(ErrorCode code) {
                Toast.makeText(context,"网络连接失败",Toast.LENGTH_SHORT).show();
                isEnd = true;
                notifyDataSetChanged();
            }
        });
    }
    @Override
    public int getItemViewType(int position){
        if(position == data.size()){
            return ITEM_LOADING;
        }else{
            return ITEM_BOOK;
        }
    }
    @Override
    public int getItemCount(){
        return isEnd ? data.size() : data.size()+1;//如已获取不到更多信息，取消loading显示的count
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type){
        if(type == ITEM_BOOK){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item,parent,false);
            return  new BookViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_item,parent,false);
            return  new LoadingViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
     if( holder instanceof BookViewHolder){
         final BookViewHolder bookViewHolder = (BookViewHolder) holder;
         final Book book = data.get(position);
         bookViewHolder.title.setText("书名："+book.getTitle());
         bookViewHolder.author.setText(book.getAuthor());
         bookViewHolder.publicationDate.setText(book.getPublicationDate());
         bookViewHolder.publisher.setText(book.getPublisher());
         bookViewHolder.isbn.setText("ISBN："+book.getIsbn());
         //此处需要做一些说明，因为getCover方法是根据isbn号在线获取的，所以只需要执行一次
         //之后将其写入book中，如果返回为空，用空string做判定。
         if(book.getCoverUrl() == null){
             helper.getBookCover(book.getIsbn().replaceAll("-", ""), new NetworkHelper.LoadCoverCallback() {
                 @Override
                 public void onResponse(String url) {
                     url = url == null ? "" : url;
                     book.setCoverUrl(url);
                     if(url.equals("")){
                         Picasso.with(context).load(R.drawable.no_image_available).into(bookViewHolder.cover);
                     }else {
                         Picasso.with(context).load(url).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(bookViewHolder.cover);
                     }
                 }
             });
         }else {
             if (book.getCoverUrl().equals("")) {
                 Picasso.with(context).load(R.drawable.no_image_available).into(bookViewHolder.cover);
             } else {
                 Picasso.with(context).load(book.getCoverUrl()).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(bookViewHolder.cover);
             }
         }
     }else{
         if(page != 0 ){
             loadMore();
         }
     }

    }



    class BookViewHolder extends RecyclerView.ViewHolder{
        public TextView title,author, publicationDate,publisher,isbn;
        public ImageView cover;
        public BookViewHolder(final View view){
            super(view);
            title = (TextView) view.findViewById(R.id.book_item_title);
            author = (TextView) view.findViewById(R.id.book_item_author);
            publicationDate = (TextView) view.findViewById(R.id.book_item_publish_year);
            publisher = (TextView) view.findViewById(R.id.book_item_publisher);
            isbn = (TextView) view.findViewById(R.id.book_item_isbn);
            cover = (ImageView) view.findViewById(R.id.book_item_cover);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra("book",data.get(getAdapterPosition()));
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) context,view,"item").toBundle());
                    }else{
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
    class LoadingViewHolder extends RecyclerView.ViewHolder{
        public LoadingViewHolder(View v){
            super(v);
        }
    }

    /**
     * 搜索&加载的回调，用于控制相应的UI变化
     */
    public interface LoadDataCallback{
        void onSuccess();
        void onFailure(ErrorCode code);
    }
    public void cancelLoading(){
        isEnd = true;
        notifyDataSetChanged();
    }
}
