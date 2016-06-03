package com.will.sxlib;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.will.sxlib.base.BaseActivity;
import com.will.sxlib.guide.GuideFragment;
import com.will.sxlib.myBook.MyBookFragment;
import com.will.sxlib.searchBook.SearchFragment;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.UserOperationHelper;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends BaseActivity{
    public static final int SEARCH = 0;
    public static final int GUIDE = 1;
    public static final int MY_BOOK = 2;
    private DrawerLayout drawerLayout;
    private ImageButton arrow;
    private NavigationView navigationView;
    private TextView userName;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private FragmentManager fragmentManager = getFragmentManager();
    private AlertDialog loginDialog;
    private AlertDialog changePasswordDialog;
    public View statusBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeSP();
        initializeView();
        setupArrowIndex();
        setupNavigationViewClickEvent();
        switchNavigationItems(SEARCH);
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
        statusBar = findViewById(R.id.status_bar);
        RelativeLayout header = (RelativeLayout) navigationView.getHeaderView(0);
        arrow = (ImageButton) header.findViewById(R.id.drawer_header_arrow);
        userName = (TextView) header.findViewById(R.id.drawer_header_user_name);
        userName.setText(sp.getString("userName","匿名读者"));
    }
    private void showLoginDialog(){
        if(loginDialog == null){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_login,null);
            builder.setCancelable(false);
            builder.setView(view);
            loginDialog = builder.create();
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
                        final UserOperationHelper helper = UserOperationHelper.getInstance(MainActivity.this,accountStr,passwordStr);
                        helper.setUserInfo(accountStr,passwordStr);
                        helper.login(new UserOperationHelper.LoginCallback() {
                            @Override
                            public void onSuccess() {
                                writeUserInfo2SP(accountStr,passwordStr,helper.getUserName());
                                progressBar.setVisibility(View.GONE);
                                userName.setText(helper.getUserName());
                                showToast("登陆成功");
                                loginDialog.cancel();
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
                    loginDialog.cancel();
                }
            });
        }
        loginDialog.show();
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
    private void setupNavigationViewClickEvent(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_item_login:
                        if(!hasAccountInfo()){
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
                                    switchNavigationItems(SEARCH);
                                }
                            });
                            builder.setNegativeButton("取消",null);
                            builder.create().show();
                        }
                        break;
                    case R.id.navigation_item_my_book:
                        if(!hasAccountInfo()){
                            showToast("未登录！");
                        }else{
                           switchNavigationItems(MY_BOOK);
                        }
                        break;
                    case R.id.navigation_item_search:
                            switchNavigationItems(SEARCH);
                        break;
                    case R.id.navigation_item_guide:
                            switchNavigationItems(GUIDE);
                        break;
                    case R.id.navigation_item_change_password:
                        if(hasAccountInfo()){
                            showChangePasswordDialog();
                        }else{
                            showToast("未登录!");
                        }
                        break;
                }
                return true;
            }
        });
    }
    public void switchNavigationItems(int which){
        Fragment fragment;
       switch (which){
           case SEARCH :
               fragment = fragmentManager.findFragmentByTag("search");
               if(fragment == null) {
                   fragmentManager.beginTransaction().setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                           .add(R.id.fragment_container, new SearchFragment(), "search")
                           .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
               }else{
                   if (!fragment.isVisible()){
                       fragmentManager.beginTransaction().setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                               .show(fragment)
                               .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                   }
               }hideOtherFragments(which);
               drawerLayout.closeDrawer(GravityCompat.START);
               statusBar.setBackgroundResource(R.drawable.status_bar_bg);
               break;
           case GUIDE :
               fragment = fragmentManager.findFragmentByTag("guide");
               if(fragment == null) {
                   fragmentManager.beginTransaction().setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                           .add(R.id.fragment_container, new GuideFragment(), "guide").commit();
               }else{
                   if (!fragment.isVisible()){
                       fragmentManager.beginTransaction().setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                               .show(fragment).commit();
                   }
               }
               hideOtherFragments(which);
               statusBar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
               drawerLayout.closeDrawer(GravityCompat.START);
               break;
           case MY_BOOK:
               fragment = fragmentManager.findFragmentByTag("myBook");
               if(fragment == null) {
                   fragmentManager.beginTransaction().setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                           .add(R.id.fragment_container, new MyBookFragment(), "myBook").commit();
               }else{
                   if (!fragment.isVisible()){
                       fragmentManager.beginTransaction().setCustomAnimations(R.animator.animator_in,R.animator.animator_out)
                               .show(fragment).commit();
                   }
               }
               hideOtherFragments(which);
               statusBar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
               drawerLayout.closeDrawer(GravityCompat.START);
               break;
       }
    }
    private void hideOtherFragments(int which){
        switch (which){
            case 0:
                if(fragmentManager.findFragmentByTag("guide") != null) {
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("guide")).commit();
                }
                if(fragmentManager.findFragmentByTag("myBook") != null){
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("myBook")).commit();
                }
                break;
            case 1:
                if(fragmentManager.findFragmentByTag("search") != null) {
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("search")).commit();
                }
                if(fragmentManager.findFragmentByTag("myBook") != null){
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("myBook")).commit();
                }
                break;
            case 2:
                if(fragmentManager.findFragmentByTag("guide") != null) {
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("guide")).commit();
                }
                if(fragmentManager.findFragmentByTag("search") != null){
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("search")).commit();
                }
                break;
        }
    }
    private void writeUserInfo2SP(String account,String password,String userName){
        editor.putString("account",account);
        editor.putString("password",password);
        editor.putString("userName",userName);
        editor.apply();
    }

    /**
     * 初始化sharedPreferences相关
     */
    private void initializeSP(){
        if(sp == null){
            sp = getSharedPreferences("config",MODE_PRIVATE);
            editor = sp.edit();
        }
    }
    private void showChangePasswordDialog(){
        if(changePasswordDialog == null){
            final SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password,null);
            builder.setView(view);
            builder.setCancelable(false);
            changePasswordDialog = builder.create();
            final EditText oldEdit = (EditText) view.findViewById(R.id.change_password_dialog_old_password);
            final EditText newEdit_1 = (EditText) view.findViewById(R.id.change_password_dialog_new_password_1);
            final EditText newEdit_2 = (EditText) view.findViewById(R.id.change_password_dialog_new_password_2);
            final SmoothProgressBar progressBar = (SmoothProgressBar) view.findViewById(R.id.change_password_dialog_progress_bar);
            Button cancel = (Button) view.findViewById(R.id.change_password_dialog_cancel);
            Button confirm = (Button) view.findViewById(R.id.change_password_dialog_confirm);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePasswordDialog.cancel();
                }
            });
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String oldStr = oldEdit.getText().toString();
                    String newStr_1 = newEdit_1.getText().toString();
                    String newStr_2 = newEdit_2.getText().toString();
                    if(oldStr.isEmpty() || newStr_1.isEmpty() || newStr_2.isEmpty()){
                        showToast("密码不能为空！");
                    }else if(!newStr_1.equals(newStr_2)){
                        showToast("两次新密码输入不同!");
                    }else{
                        progressBar.setVisibility(View.VISIBLE);
                        UserOperationHelper helper = UserOperationHelper
                                .getInstance(MainActivity.this,sp.getString("account",""),sp.getString("password",""));
                        helper.changePassword(oldStr, newStr_1, new UserOperationHelper.ChangePasswordCallback() {
                            @Override
                            public void onSuccess() {
                                oldEdit.setText("");
                                newEdit_1.setText("");
                                newEdit_2.setText("");
                                changePasswordDialog.cancel();
                                showToast("修改成功");
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(ErrorCode code) {
                               if(code == ErrorCode.CONNECTION_FAILED){
                                   showToast("连接失败");
                               }else if(code == ErrorCode.OLD_PASSWORD_INVALID){
                                   showToast("原密码输入错误");
                               }else if (code == ErrorCode.PASSWORD_INVALID){
                                   showToast("密码已被修改，请重新登录");
                                   logout();
                               }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }
        changePasswordDialog.show();
    }
    private boolean hasAccountInfo(){
        if(sp == null){
            sp  = getSharedPreferences("config",MODE_PRIVATE);
        }
        return !sp.getString("account","").isEmpty();
    }
    public void logout(){
        getSharedPreferences("config",MODE_PRIVATE).edit().clear().apply();
        userName.setText("匿名用户");
    }
}
