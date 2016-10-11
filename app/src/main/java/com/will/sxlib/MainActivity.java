package com.will.sxlib;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.will.sxlib.base.BaseActivity;
import com.will.sxlib.base.MyFragments;
import com.will.sxlib.guide.GuideFragment;
import com.will.sxlib.myBook.MyBookFragment;
import com.will.sxlib.searchBook.SearchFragment;
import com.will.sxlib.util.ErrorCode;
import com.will.sxlib.util.FragmentSwitcher;
import com.will.sxlib.util.SPHelper;
import com.will.sxlib.util.UserOperationHelper;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import static android.R.attr.fragment;
import static com.will.sxlib.base.MyFragments.GUIDE;
import static com.will.sxlib.base.MyFragments.MY_BOOK;
import static com.will.sxlib.base.MyFragments.SEARCH;

public class MainActivity extends BaseActivity{
    /*
    public static final int SEARCH = 0;
    public static final int GUIDE = 1;
    public static final int MY_BOOK = 2;
    public static final int MY_FAVORITE = 3;
    */
    private DrawerLayout drawerLayout;
    private ImageButton arrow;
    private NavigationView navigationView;
    private TextView userName;
    private AlertDialog loginDialog;
    private AlertDialog changePasswordDialog;
    private View statusBar;

    private FragmentSwitcher fragmentSwitcher;

    private MyFragments selectedItem = SEARCH;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
        setupArrowIndex();
        setupNavigationViewClickEvent();
        switchNavigationItems(SEARCH);
        ///new NotificationChecker(this).check();
    }

    /**
     * 更改drawer的状态,方便从fragment中调用
     */
    public void changeDrawerState(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * 初始化view
     */
    private void initializeView(){
        fragmentSwitcher = new FragmentSwitcher(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        statusBar = findViewById(R.id.status_bar);
        RelativeLayout header = (RelativeLayout) navigationView.getHeaderView(0);
        arrow = (ImageButton) header.findViewById(R.id.drawer_header_arrow);
        userName = (TextView) header.findViewById(R.id.drawer_header_user_name);
        userName.setText(SPHelper.getUserName());
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                switchNavigationItems(selectedItem);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    /**
     * 展示登录dialog，保存dialog实例
     */
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
                        final UserOperationHelper helper = UserOperationHelper.getInstance(accountStr,passwordStr);
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

    /**
     * 初始化header中的箭头按钮，点击控制登录/修改密码这两个item的出现与隐藏事件。
     */
    private void setupArrowIndex(){
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.isSelected()){
                    v.setSelected(false);
                    TransitionManager.beginDelayedTransition(navigationView,new Fade().setDuration(200));
                    navigationView.getMenu().setGroupVisible(R.id.navigation_group_user,true);
                }else{
                    v.setSelected(true);
                    TransitionManager.beginDelayedTransition(navigationView,new Fade().setDuration(200));
                    navigationView.getMenu().setGroupVisible(R.id.navigation_group_user,false);
                }
            }
        });
    }

    /**
     * 处理drawer中item的点击事件
     */
    private void setupNavigationViewClickEvent(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_item_my_book:
                        if(!hasAccountInfo()){
                            showToast("未登录！");
                        }else{
                            selectedItem = MY_BOOK;
                            drawerLayout.closeDrawers();
                        }
                        break;
                    case R.id.navigation_item_search:
                            selectedItem = SEARCH;
                            drawerLayout.closeDrawers();
                        break;
                    case R.id.navigation_item_guide:
                            selectedItem = GUIDE;
                            drawerLayout.closeDrawers();
                        break;
                    case R.id.navigation_item_my_favorite:
                            selectedItem = MyFragments.MY_FAVORITE;
                            drawerLayout.closeDrawers();
                        break;

                    case R.id.navigation_item_change_password:
                        if(hasAccountInfo()){
                            showChangePasswordDialog();
                        }else{
                            showToast("未登录!");
                        }
                        break;

                    case R.id.navigation_item_login:
                        if(!hasAccountInfo()){
                            showLoginDialog();
                        }else{
                            showLogoutDialog();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 展示注销界面，这个方法用于在login/logout的点击事件中，检测到用户已登录时调用。
     */
    private void showLogoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("退出确认");
        builder.setMessage("确定要退出账号登陆吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               SPHelper.clearConfig();
                userName.setText("匿名读者");
                showToast("已退出");
                switchNavigationItems(SEARCH);
            }
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();
    }

    /**
     * 控制item之间的跳转，因为并没有用replace处理fragment的切换，所以代码有些冗余，
     * 但这样可以避免fragment的重复创建，提升用户体验
     * @param which 被点击的item
     */
    public void switchNavigationItems(MyFragments which){
        fragmentSwitcher.switchTo(which);
    }


    /**
     * 将user信息写入sharedPreferences，在登录dialog中调用
     * @param account 账号
     * @param password 密码
     * @param userName 用户名
     */
    private void writeUserInfo2SP(String account,String password,String userName){
        SPHelper.setAccount(account);
        SPHelper.setPassword(password);
        SPHelper.setUserName(userName);
    }


    /**
     * 展示修改密码dialog
     */
    private void showChangePasswordDialog(){
        if(changePasswordDialog == null){
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
                                .getInstance(SPHelper.getAccount(),SPHelper.getPassword());
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

    /**
     * 检验sharedPreferences中是否有账号信息，在打开需要用户登录的界面之前调用本方法。
     * @return 是否有账号信息
     */
    private boolean hasAccountInfo(){
        return !SPHelper.getAccount().isEmpty();
    }

    /**
     * 退出登录，删除sharedPreferences中的信息，将header中的userName修改为匿名
     */
    public void logout(){
        SPHelper.clearConfig();
        userName.setText("匿名用户");
    }
    public View getStatusBar(){
        return statusBar;
    }


    private Toast exitToast;
    /**
     * back键点击事件，首次back显示一个toast，再按一次则退出。
     */
    private void showExitToast(){
        if(exitToast == null){
            exitToast = Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT);
        }
            if(exitToast.getView().getParent() == null){
                exitToast.show();
            }else{
                super.onBackPressed();
            }
    }
    @Override
    public void onBackPressed(){
        showExitToast();
    }
}
