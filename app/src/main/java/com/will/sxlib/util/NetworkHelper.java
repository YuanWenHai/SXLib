package com.will.sxlib.util;

import android.content.Context;
import android.os.Handler;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bean.BookState;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Will on 2016/5/20.
 * 书籍相关数据获取工具类
 */
public class NetworkHelper {
    /**
     * 正序
     */
    public static final int SORT_BY_DATE_ASC = 2;
    /**
     * 倒序
     */
    public static final int SORT_BY_DATE_DESC = 1;
    /**
     * 匹配度
     */
    public static final int SORT_BY_MATCHING = 0;

    public static final int SEARCH_BY_TITLE = 3;

    public static final int SEARCH_BY_AUTHOR = 4;

    public static final int SEARCH_BY_DEFAULT = 5;
    private static final String SEARCH_COVER_URL = "http://api.interlib.com.cn/interlibopac/websearch/metares?cmdACT=getImages&isbns=";//加上isbn
    private static final String SEARCH_STATE_URL = "http://opac.lib.sx.cn/opac/api/holding/";//加code
    private static final String KEY_SEARCH_URL_PART_1 = "http://opac.lib.sx.cn/opac/search?q=";//加上关键字
    //根据匹配度排序-----最后加上页数
    private static final String KEY_SEARCH_URL_PART_2_SORT_BY_MATCHING = "&&searchType=standard&isFacet=true&view=standard&rows=10&sortWay=score&sortOrder=desc&hasholding=1&f_curlibcode=0101&searchWay0=marc&q0=&logical0=AND&page=";
    //根据发布日期---加上asc为升序，desc为降序
    private static final String KEY_SEARCH_URL_PART_2_SORT_BY_DATE = "&searchType=standard&isFacet=true&view=standard&rows=10&sortWay=pubdate_sort&sortOrder=";
    //这里加上页数
    private static final String KEY_SEARCH_URL_PART_3_SORT_BY_DATE = "&hasholding=1&f_curlibcode=0101&searchWay0=marc&q0=&logical0=AND&page=";
    //升降序,desc/asc
    private static final String SEARCH_SEGMENT_SORT_ORDER_4 = "&sortOrder=";
    //按照时间排序
    private static final String SEARCH_SEGMENT_SORT_WAY_3 = "&rows=10&curlibcode=0101&hasholding=1&searchWay0=marc&q0=&logical0=AND&sortWay=";
    /**
     * 搜索规则,匹配度/score；作者/author;题名/title
     */
    private static final String SEARCH_SEGMENT_SEARCH_WAY_2 = "&searchType=standard&isFacet=true&view=standard&searchWay=";
    //页数
    private static final String SEARCH_SEGMENT_SEARCH_PAGE_5 ="&page=";
    private static final String SEARCH_HOST_1 = "http://opac.lib.sx.cn/opac/search?q=";
    private  OkHttpClient client;
    private Handler handler;
    private Context context;
    private int pageCount = 1;//页数
    //临时list，用于缓存book，因为进行了过滤，每次请求返回的bookList数肯定低于十条。在此缓存够十条后一次性交给调用者
    private List<Book> temp = new ArrayList<>();
    private static NetworkHelper instance;
    private NetworkHelper(Context context){
        client = new OkHttpClient();
        handler = new Handler(context.getMainLooper());
        this.context = context;
    }
    public static NetworkHelper getInstance(Context context){
        if(instance == null){
            instance =  new NetworkHelper(context);
        }
            return instance;
        }

    /**
     * 关键字搜索
     * @param key 关键词
     * @param page 页数
     * @param sort 排序方式
     * @param callback callback
     */
    public void keywordSearch(String key, int page,int sort,int searchWay, final KeySearchCallback callback){
        StringBuilder builder = new StringBuilder();
        builder.append(SEARCH_HOST_1).append(key);
        if(searchWay == SEARCH_BY_AUTHOR){
            builder.append(SEARCH_SEGMENT_SEARCH_WAY_2).append("author");
        }else if (searchWay == SEARCH_BY_TITLE){
            builder.append(SEARCH_SEGMENT_SEARCH_WAY_2).append("title");
        } else{
            builder.append(SEARCH_SEGMENT_SEARCH_WAY_2);
        }
        if(sort == SORT_BY_DATE_ASC){
            //升序,ascending
            builder.append(SEARCH_SEGMENT_SORT_WAY_3).append("pubdate_sort").append(SEARCH_SEGMENT_SORT_ORDER_4)
                    .append("asc").append(SEARCH_SEGMENT_SEARCH_PAGE_5).append(page);
        }else if (sort == SORT_BY_DATE_DESC){
            //降序,descending
            builder.append(SEARCH_SEGMENT_SORT_WAY_3).append("pubdate_sort").append(SEARCH_SEGMENT_SORT_ORDER_4)
                    .append("desc").append(SEARCH_SEGMENT_SEARCH_PAGE_5).append(page);
        }else if (sort == SORT_BY_MATCHING){
            //默认搜索，根据匹配度排列
            builder.append(SEARCH_SEGMENT_SORT_WAY_3).append("score").append(SEARCH_SEGMENT_SORT_ORDER_4)
                    .append("desc").append(SEARCH_SEGMENT_SEARCH_PAGE_5).append(page);
        }
        String url = builder.toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(ErrorCode.CONNECTION_FAILED);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                JsonUtil.getBookListFromHtml(response.body().string(), new JsonUtil.BookListCallback() {
                    @Override
                    public void onResult(final List<Book> list, final String total) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(list,total);
                            }
                        });
                    }
                });

            }
        });
    }


    /**
     * 从网页抓取图书详情
     * @param bookNumber bookNumber
     * @param callback callback
     */
    public void loadBookDetail(String bookNumber,final LoadDetailCallback callback){
        String url = "http://opac.lib.sx.cn/opac/book/"+bookNumber;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(ErrorCode.CONNECTION_FAILED);
                    }
                });
            }
            @Override
            public void onResponse(Response response) throws IOException {
                final HashMap<String,String> map = new HashMap<>();
                Document doc = Jsoup.parse(response.body().string());
                final Elements elements = doc.select("table#bookInfoTable");
                final String title = elements.select("h2").text();
                Elements trs = elements.select("tr");
                for(Element element :trs){
                    map.put(element.select(".leftTD").text(),element.select(".rightTD").text());
                    //Log.e(element.select(".leftTD").text(),element.select(".rightTD").text());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(title,map);
                    }
                });
            }
        });
    }

    /**
     * 获取书籍封面图片链接。
     * 此处，有些问题，在高并发的情况下，有很大概率会出现request失败，
     * 所以在失败回调中重新执行request，
     *但这也会导致一些新的问题，有些错误无法反馈给调用者
     * @param isbn isbn
     * @param callback callback
     */
    public void getBookCover(final String isbn, final LoadCoverCallback callback){
        Request request = new Request.Builder().url(SEARCH_COVER_URL +isbn).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            //此处，有些问题，在高并发的情况下，有很大概率会出现失败
            // 所以在失败回调中重新执行request
            //但这也会导致一些新的问题，有些错误无法反馈给调用者
            public void onFailure(Request request, IOException e) {
                //callback.onFailure(ErrorCode.CONNECTION_FAILED);
                try{
                    final String link = JsonUtil.getCoverUrl(client.newCall(request).execute().body().string());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(link);
                        }
                    });
                }catch (IOException i ){
                    i.printStackTrace();
                }
            }
            @Override
            public void onResponse(Response response) throws IOException {
                final String link = JsonUtil.getCoverUrl(response.body().string());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(link);
                    }
                });
            }
        });

    }

    /**
     *获取书籍借还与馆藏状态
     * @param code 书籍代码，例如2001329964
     * @param callback  callback
     */
    public void getBookState(String code, final LoadStateCallback callback){
        final Request request = new Request.Builder().url(SEARCH_STATE_URL +code).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(ErrorCode.CONNECTION_FAILED);
                    }
                });
            }
            @Override
            public void onResponse(Response response) throws IOException {
                final List<BookState> states = JsonUtil.getBookState(response.body().string(),context);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(states);
                    }
                });
            }
        });
    }
    public void getResourceIntro(final LoadIntroCallback callback){
        Request request = new Request.Builder().url("http://lib.sx.cn/html/1/dzzn/32.html").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }
            @Override
            public void onResponse(Response response) throws IOException {
                Document doc = Jsoup.parse(response.body().string());
                final String article = doc.select(".article").html();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(article);
                    }
                });
            }
        });
    }

    public interface LoadIntroCallback{
        void onSuccess(String html);
        void onFailure();
    }
    public interface LoadDetailCallback {
        /**
         *
         * @param title 书籍标题
         * @param map 网页内容映射，键值对应，例如：map.get("ISBN:")
         */
        void onResponse(String title,Map<String,String> map);
        void onFailure(ErrorCode code);
    }
    public interface LoadCoverCallback {
        /**
         *
         * @param url cover的下载链接，得到后直接用picasso载入。需要注意的是，如果没有封面信息，则会返回null，使用时必须进行非null判定！
         *
         */
        void onResponse(String url);
    }
    public interface LoadStateCallback{
        /**
         *
         * @param bookState 单条或多条馆藏记录，若状态为借出，则loanData为null
         */
        void onResponse(List<BookState> bookState);
        void onFailure(ErrorCode code);
    }
    public interface KeySearchCallback{
        void onResponse(List<Book> books,String total);
        void onFailure(ErrorCode code);
    }
}
