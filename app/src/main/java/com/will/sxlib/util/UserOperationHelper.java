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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public UserOperationHelper(Context context,String account,String password){
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
                loginCallback.onFailure(ErrorCode.CONNECTION_FAILED);
            }
            @Override
            public void onResponse( Response response) throws IOException {
               if(!response.toString().contains("http://opac.lib.sx.cn/opac/reader/doLogin")){
                   getUserNameFromHtml(response.body().string());
                   loginCallback.onSuccess();
                   Log.e("登陆成功","login success!");
               }else{
                   loginCallback.onFailure(ErrorCode.PASSWORD_INVALID);
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
                    final List<Map<Integer,String>> list = getDataFromHtml(str);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(list,headers);
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
    private List<Map<Integer,String>> getDataFromHtml(String html){
        List<Map<Integer ,String>> list = new ArrayList<>();
        Map<Integer,String> map;
        Document document = Jsoup.parse(html);
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
            map = new HashMap<>();
            for(int i = 0; i < element.children().size(); i++){
               map.put(i,element.children().get(i).text());
           }
            list.add(map);
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
         * 因为资源的问题，数据取值从1开始，0为网页上的checkBox...
         * @param mapList mapList，每个map为一行借阅数据
         * @param headerList 标题map，顺序与map中的相对应
         */
        void onResponse(List<Map<Integer,String>> mapList,List<String> headerList);
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
