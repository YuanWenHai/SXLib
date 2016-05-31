package com.will.sxlib.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.will.sxlib.bean.MyBook;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/5/23.
 */
public class UserOperationHelper {
    public static final int INVALID_PASSWORD = 0;
    public static final int CONNECTION_ERROR = 1;
    public static final int RENEW_FAILED = 2;
    private static final String RENEW_URL = "http://opac.lib.sx.cn/opac/loan/renewList";//续借页面，获取借阅数据
    private static final String LOGIN_URL = "http://opac.lib.sx.cn/opac/reader/doLogin";//登陆
    private static final String DO_RENEW_URL = "http://opac.lib.sx.cn/opac/loan/doRenew";//进行续借操作
    private List<String> headers;//缓存HeaderName，避免重复操作
    private OkHttpClient userClient;
    private Handler handler;
    private String account;
    private String password;
    private String userName = "匿名用户";
    private static UserOperationHelper instance;
    public static UserOperationHelper getInstance(Context context,String account,String password){
        if(instance == null){
            instance = new UserOperationHelper(context,account,password);
        }
        return instance;
    }
    private UserOperationHelper(Context context,String account,String password){
        userClient = new OkHttpClient();
        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        userClient.setCookieHandler(manager);
        handler = new Handler(context.getMainLooper());
        this.account = account;
        this.password = md5(password);
    }
    public void login(final LoginCallback loginCallback){
        RequestBody body = new MultipartBuilder().type(MultipartBuilder.FORM)
                .addFormDataPart("rdid",account)
                .addFormDataPart("rdPasswd",password).build();
        Request request = new Request.Builder().url(LOGIN_URL).
                post(body).
                build();
        userClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("连接失败！","····");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loginCallback.onFailure(ErrorCode.CONNECTION_FAILED);
                    }
                });
            }
            @Override
            public void onResponse( Response response) throws IOException {
               if(!response.toString().contains("http://opac.lib.sx.cn/opac/reader/doLogin")){
                   getUserNameFromHtml(response.body().string());
                   handler.post(new Runnable() {
                       @Override
                       public void run() {
                           loginCallback.onSuccess();
                       }
                   });
                   Log.e("登陆成功","login success!");
               }else{
                  handler.post(new Runnable() {
                      @Override
                      public void run() {
                          loginCallback.onFailure(ErrorCode.PASSWORD_INVALID);
                      }
                  });
               }
            }
        });
    }

    /**
     * 获取借阅信息，若未登录则自行调用登陆方法
     * @param callback callback
     */
    public void getLoanData(final RenewCallback callback){
        final Request request = new Request.Builder().url(RENEW_URL).build();
        userClient.newCall(request).enqueue(new Callback() {
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
                String str = response.body().string();
                if(isLogined(str)){
                    final List<MyBook> list = getDataFromHtml(str);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(list);
                        }
                    });
                }else{
                    login(new LoginCallback() {
                        @Override
                        public void onSuccess() {
                            getLoanData(callback);
                        }
                        @Override
                        public void onFailure(final ErrorCode code) {
                         handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 callback.onFailure(code);
                             }
                         });
                        }
                    });
                }
            }
        });
    }

    /**
     * 执行图书续借
     * @param code 图书代码，例如 01011013750415
     * @param callback 回调
     */
    public void renewBook(final String code, final DoRenewCallback callback){
        RequestBody body = new MultipartBuilder().type(MultipartBuilder.FORM)
                .addFormDataPart("barcodeList",code)
                .addFormDataPart("furl","/opac/loan/renewList").build();
        final Request request = new Request.Builder().url(DO_RENEW_URL).post(body).build();
        userClient.newCall(request).enqueue(new Callback() {
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
                //如果返回以下重定向连接，说明当前状态为未登录，调用登陆方法
                if(response.toString().contains("http://opac.lib.sx.cn/opac/reader/login?returnUrl=/loan/doRenew")){
                    login(new LoginCallback() {
                        @Override
                        public void onSuccess() {
                            renewBook(code,callback);
                        }
                        @Override
                        //所有对外公开的callback都应该放入MainThread
                        public void onFailure(final ErrorCode code) {
                           handler.post(new Runnable() {
                               @Override
                               public void run() {
                                   callback.onFailure(code);
                               }
                           });
                        }
                    });
                }else{
                    final String str = response.body().string();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(renewResult(str)){
                                callback.onSuccess();
                            }else{
                                callback.onFailure(ErrorCode.RENEW_FAILED);
                            }
                        }
                    });
                }

            }
        });
    }
    public String getUserName(){
        return userName;
    }
    /**
     * 提取html中内容，用list<map>返回
     * @param html htmlString
     * @return listMap； 每个map代表一行借阅数据
     */
    private List<MyBook> getDataFromHtml(String html){
        List<MyBook> list = new ArrayList<>();
        Document document = Jsoup.parse(html);
        MyBook myBook;
        Elements table = document.select("table#contentTable");
        Elements tr = table.select("tr");
        for (Element element :tr){
            //如果是标题数据，缓存，如已有缓存，跳过
            if(element.id().equals("contentHeader")){
                if(headers != null){
                    continue;
                }else{
                    headers = new ArrayList<>();
                    for(Element child :element.children()){
                        headers.add(child.text());
                    }
                    continue;
                }
            }
            myBook = new MyBook();
            Elements children  = element.children();
            myBook.setBarCode(headers.get(1)+": "+children.get(1).text());
            myBook.setTitle(headers.get(2)+": "+children.get(2).text());
            myBook.setCallNo(headers.get(3)+": "+children.get(3).text());
            myBook.setLocal(headers.get(4)+": "+children.get(4).text());
            myBook.setType(headers.get(5)+": "+children.get(5).text());
            myBook.setVolumeInfo(headers.get(6)+": "+children.get(6).text());
            myBook.setLoanDate(headers.get(7)+": "+children.get(7).text());
            myBook.setReturnDate(headers.get(8)+": "+children.get(8).text());
            myBook.setRenewCount(headers.get(9)+": "+children.get(9).text());
            list.add(myBook);
        }
        return list;
    }

    /**
     *  通过获取的页面string检验是否登陆
     * @param html
     * @return
     */
    private boolean isLogined(String html){
        Document document = Jsoup.parse(html);
        return !document.select("table#contentTable").isEmpty();
    }

    /**
     * 通过返回的页面string检验是否续借成功
     * @param html
     * @return
     */
    private boolean renewResult(String html){
        Document document = Jsoup.parse(html);
        String content = document.select("div#content").text();
        return !content.contains("对不起");
    }

    /**
     * 从html中提取用户名
     * @param html
     */
    private void getUserNameFromHtml(String html){
        Document document = Jsoup.parse(html);
        userName = document.select("div.navbar_info_zh").text().replace("欢迎您：","").replace(" ","").replace("退出","");
    }
    public interface RenewCallback{
        /**
         *
         * @param list bookList
         */
        void onResponse(List<MyBook> list);
        void onFailure(ErrorCode code);
    }

    /**
     *登陆状况回调,
     */
    public interface LoginCallback{
        void onSuccess();
        void onFailure(ErrorCode code);
    }

    /**
     * 续借状态回调
     */
    public interface DoRenewCallback{
        void onSuccess();
        /**
         *
         * @param code 状态码
         */
        void onFailure(ErrorCode code);
    }




    private  String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
