package com.will.sxlib;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.will.sxlib.base.BaseActivity;
import com.will.sxlib.searchBook.SearchFragment;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.UserOperationHelper;

import java.util.List;
import java.util.Map;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends BaseActivity{
    private DrawerLayout drawerLayout;
    private ImageButton arrow;
    private NavigationView navigationView;
    private TextView userName;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
        setupArrowIndex();
        switchNavigationView();
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,new SearchFragment()).commit();
    }
    public void changeDrawerState(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
    private void initializeView(){
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        arrow = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_arrow);
        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_user_name);
        if(isLogined()){
            userName.setText(sp.getString("userName",""));
        }
    }
    private void showLoginDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.login_dialog,null);
        builder.setCancelable(false);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        final EditText accountEdit = (EditText) view.findViewById(R.id.login_dialog_account);
        final EditText passwordEdit = (EditText) view.findViewById(R.id.login_dialog_password);
        Button login = (Button) view.findViewById(R.id.login_dialog_login);
        Button cancel = (Button) view.findViewById(R.id.login_dialog_cancel);
        final SmoothProgressBar progressBar = (SmoothProgressBar) view.findViewById(R.id.login_dialog_progress_bar);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String accountStr = accountEdit.getText().toString();
                final String passwordStr = passwordEdit.getText().toString();
                if(!accountStr.isEmpty() && !passwordStr.isEmpty()){
                    progressBar.setVisibility(View.VISIBLE);
                    final UserOperationHelper helper = new UserOperationHelper(MainActivity.this,accountStr,passwordStr);
                    helper.login(new UserOperationHelper.LoginCallback() {
                        @Override
                        public void onSuccess() {
                            writeUserInfo2SP(accountStr,passwordStr,helper.getUserName());
                            progressBar.setVisibility(View.GONE);
                            userName.setText(helper.getUserName());
                            showToast("登陆成功");
                            dialog.cancel();
                        }

                        @Override
                        public void onFailure(ErrorCode code) {
                            progressBar.setVisibility(View.GONE);
                            if(code == ErrorCode.CONNECTION_FAILED){
                                showToast("网络连接失败");
                            }else if (code == ErrorCode.PASSWORD_INVALID){
                                showToast("账号与密码不符");
                            }
                        }
                    });
                }else{
                    showToast("账号或密码为空");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }
    private void setupArrowIndex(){
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.isSelected()){
                    v.setSelected(false);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        TransitionManager.beginDelayedTransition(navigationView,new Slide().setDuration(100));
                    }else{
                        TransitionManager.beginDelayedTransition(navigationView,new Fade().setDuration(100));
                    }
                    navigationView.getMenu().setGroupVisible(R.id.navigation_group_user,true);
                }else{
                    v.setSelected(true);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        TransitionManager.beginDelayedTransition(navigationView,new Slide().setDuration(100));
                    }else{
                        TransitionManager.beginDelayedTransition(navigationView,new Fade().setDuration(100));
                    }
                    navigationView.getMenu().setGroupVisible(R.id.navigation_group_user,false);
                }
            }
        });
    }
    private void switchNavigationView(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_item_login:
                        if(!isLogined()){
                            showLoginDialog();
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("退出确认");
                            builder.setMessage("确定要退出账号登陆吗？");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sp.edit().clear().apply();
                                    userName.setText("匿名读者");
                                    showToast("已退出");
                                }
                            });
                            builder.setNegativeButton("取消",null);
                            builder.create().show();
                        }
                        break;
                    case R.id.navigation_item_my_book:
                        new UserOperationHelper(MainActivity.this,"01010001906773","6557150").getLoanData(new UserOperationHelper.RenewCallback() {
                            @Override
                            public void onResponse(List<Map<Integer, String>> mapList, List<String> headerList) {

                            }

                            @Override
                            public void onFailure(ErrorCode code) {

                            }
                        });
                }
                return true;
            }
        });
    }
    private void writeUserInfo2SP(String account,String password,String userName){
        if(editor == null){
            sp = getSharedPreferences("config",MODE_PRIVATE);
            editor = getSharedPreferences("config",MODE_PRIVATE).edit();
        }
        editor.putString("account",account);
        editor.putString("password",password);
        editor.putString("userName",userName);
        editor.apply();
    }
    private boolean isLogined(){
        if(sp == null){
            sp = getSharedPreferences("config",MODE_PRIVATE);
        }
        return !sp.getString("account","").equals("");
    }
}
