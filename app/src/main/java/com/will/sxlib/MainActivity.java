package com.will.sxlib;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.will.sxlib.searchBook.NewSearchFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,new NewSearchFragment()).commit();
    }
}
