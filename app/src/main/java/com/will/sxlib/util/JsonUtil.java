package com.will.sxlib.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.will.sxlib.R;
import com.will.sxlib.bean.Book;
import com.will.sxlib.bean.BookState;
import com.will.sxlib.bean.Gcxx;
import com.will.sxlib.bean.LoanData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Will on 2016/5/20.
 * Json相关处理工具类
 */
public class JsonUtil {
    private static Map<String,String> localMap;
    private static Map<String,String> codeMap;

    /**
     * 将搜索得到的json转换为bookList
     * @param jsonString json格式的搜索结果
     * @return bookList
     */
    public static List<Book> jsonStr2BookList(String jsonString){
        JsonElement element = new JsonParser().parse(jsonString);
        JsonArray jsonArray = element.getAsJsonArray();
        Gson gson = new Gson();
        List<Gcxx> gcxxList;
        List<Book> bookList = new ArrayList<>();
        for(int i = 0;i<jsonArray.size();i++){
            gcxxList = new ArrayList<>();
            JsonObject object = jsonArray.get(i).getAsJsonObject();
            //Log.e("object",object.toString());
            if(!object.get("gcxx").isJsonArray()){
                continue;
            }
            JsonArray array = object.get("gcxx").getAsJsonArray();
            //
           if(array != null && array.size() != 0){
               //
               for(int t = 0;t < array.size();t++) {
                   if ((array.get(t).getAsJsonObject().get("curlibName").toString()).contains("山西省图书馆")) {
                       gcxxList.add(gson.fromJson(array.get(t), Gcxx.class));
                       //Log.e("curlibName",array.get(t).getAsJsonObject().get("curlibName").toString());
                   }
               }
               //
           }
            if(gcxxList.size() > 0 ){
                Book book = gson.fromJson(jsonArray.get(i),Book.class);
                writeData2Book(book,jsonArray.get(i));
                book.setGcxx(gcxxList);
                bookList.add(book);
                //Log.e("author",jsonArray.get(i).getAsJsonObject().get("authorList").getAsJsonArray().get(0).toString());
            }
        }
        return bookList;
    }

    /**
     *  因为json格式的原因，下列四项数据无法直接得到，需要专门提取
     * @param book 将要写入的book
     * @param element book对应的JsonElement
     * @return 写入作者，索引号，类别，isbn后的book
     */
    private static Book writeData2Book(Book book,JsonElement element){
        JsonObject object = element.getAsJsonObject();
        //若无信息，jsonArray的大小为0
        if(object.get("authorList").getAsJsonArray().size() > 0){
            book.setAuthor(object.get("authorList").getAsJsonArray().get(0).toString().replace("\"",""));
        }
        if(object.get("classnoList").getAsJsonArray().size() > 0){
            book.setIndex(object.get("classnoList").getAsJsonArray().get(0).toString().replace("\"",""));
        }
        if(object.get("subjectList").getAsJsonArray().size() > 0){
            book.setType(object.get("subjectList").getAsJsonArray().get(0).toString().replace("\"",""));
        }
        if(object.get("isbnList").getAsJsonArray().size() > 0){
            book.setIsbn(object.get("isbnList").getAsJsonArray().get(0).toString().replace("\"",""));
        }
        return book;
    }

    /**
     * 从网页中获取搜索条目
     * @param html html
     * @param callback 两条数据，搞了个callback
     */
    public static void getBookListFromHtml(String html,BookListCallback callback){
        List<Book> list = new ArrayList<>();
        Book book;
        String total;
        Document document = Jsoup.parse(html);
        Elements li = document.select("ol#curlibcodeFacetUL").select("li");
        total = li.get(0).select("span").text().replace("(","").replace(")","").replace("[山西省图书馆]","");
        Elements resultTable = document.select("table.resultTable");
        Elements tr = resultTable.select("tr");
        for(Element element :tr) {
            book = new Book();
            //book.setCoverUrl(element.select("td.coverTD").select("img[src]").text());
            Element temp = element.select("div[express_bookrecno]").first();
            book.setBookNumber(temp.attr("express_bookrecno"));
            book.setIsbn(temp.attr("express_isbn"));
            Elements bookMeta  = element.select("div.bookmeta");
            Elements div = bookMeta.select("div");
            for(int i = 0;i < div.size(); i++){
               if(i == 1){
                   book.setTitle(div.get(i).select("span.bookmetaTitle").text());
               }else if (i == 2){
                   book.setAuthor(div.get(i).text());
               }else if (i == 3){
                   String whole = div.get(i).text();
                   String publisher = whole.substring(0,whole.indexOf("出版日期"));
                   String publishDate = whole.substring(whole.indexOf("出版日期"));
                   book.setPublisher(publisher);
                   book.setPublicationDate(publishDate);
               }else if (i == 4){
                   book.setType(div.get(i).text().replace(", 索书号:",""));
               }
            }
            list.add(book);
        }
        callback.onResult(list,total);
    }

    /**
     *
     * @param json 封面相关json
     * @return 图片下载链接/如果无资源，则返回null
     * json示例： ({"result":[{"metaResID":null,"isbn":"9787202068168","coverlink":"
     * http://img1.doubanio.com/lpic/s24225080.jpg","handleTime":1463928622886,"fromRes":null,"status":0}]})
     */
    public static String getCoverUrl(String json){
        json = json.replace("(","");
        json = json.replace(")","");
        JsonElement element = new JsonParser().parse(json);
        JsonElement result = element.getAsJsonObject().get("result");
        JsonArray array = result.getAsJsonArray();
        if(array.size() > 0){
            return array.get(0).getAsJsonObject().get("coverlink").toString().replace("\"","");
        }else{
            return null;
        }
    }

    /**
     * 将得到的jsonString转换为bookStateList
     * @param json jsonString
     * @param context 初始化map时需要用context获取resource
     * @return bookStateList，此处需要注意的是，如果state为借出，那么book中的loanData即为null
     */
    public static List<BookState> getBookState(String json,Context context){
        initializeMaps(context);
        List<BookState> list = new ArrayList<>();
        JsonObject whole = new JsonParser().parse(json).getAsJsonObject();
        JsonObject loanWorkMap = whole.get("loanWorkMap").getAsJsonObject();
        JsonArray holdingList = whole.get("holdingList").getAsJsonArray();
        if(holdingList.size() > 0){
            for(JsonElement element :holdingList){
                BookState state = new Gson().fromJson(element,BookState.class);
                if(!state.getCurlib().contains("0101")){
                    continue;
                }
                LoanData data = new Gson().fromJson(loanWorkMap.get(state.getBarcode()),LoanData.class);
                if(data != null){
                    convertTime2String(data);
                    state.setLoanData(data);
                }
                list.add(state);
            }
            convertCode2State(list);
        }
        return list;
    }
    /**
     * 将图书馆、馆藏、状态信息写入map
     * @param list list
     * @return 写入后的list
     */
    private static  List<BookState> convertCode2State(List<BookState> list){
        for(BookState state : list){
            state.setCurlib("山西省图书馆");
            //Log.e("curlocalCode",state.getCurlocal());
            state.setCurlocal(localMap.get(state.getCurlocal()));
            state.setState(codeMap.get(state.getState()).replace("\"",""));
        }
        return list;
    }

    /**
     * 初始化映射表，借还信息中的code需要按照map解读
     * @param context 获取resource用
     */
    private static void initializeMaps(Context context){
        if(localMap == null){
            localMap = new HashMap<>();
            JsonArray array = new JsonParser().parse(context.getResources().getString(R.string.lib_local)).getAsJsonArray();
            for(JsonElement element : array){
                JsonObject object = element.getAsJsonObject();
                localMap.put(object.get("localcode").toString().replace("\"",""),object.get("name").toString().replace("\"",""));
                //Log.e(object.get("localcode").toString(),object.get("name").toString().replace("\"",""));
            }
        }
        if(codeMap == null){
            codeMap = new HashMap<>();
            JsonObject object = new JsonParser().parse(context.getResources().getString(R.string.state_code)).getAsJsonObject();
            for(int i = 0; i < 15;i++){
                JsonObject temp = object.get(String.valueOf(i)).getAsJsonObject();
                codeMap.put(temp.get("stateType").toString(),temp.get("stateName").toString());
            }
        }
    }

    /**
     * 将时间戳转换为年月日格式
     * @param loanData 只有long时间戳的loanData
     * @return 写入String时间的loanData
     */
    private static LoanData convertTime2String(LoanData loanData){
        Date loanDate = new Date(loanData.getLoanDate());
        Date returnDate = new Date(loanData.getReturnDate());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        String loanDateStr = formatter.format(loanDate);
        String returnDateStr = formatter.format(returnDate);
        loanData.setLoanDateStr(loanDateStr);
        loanData.setReturnDateStr(returnDateStr);
        return loanData;
    }

    /**
     * 因为需要传两条数据，故写一个callback
     */
    public  interface BookListCallback{
        /**
         *
         * @param list 检索得到的书目列表
         * @param total 总条数
         */
        void onResult(List<Book> list,String total);
    }
}
