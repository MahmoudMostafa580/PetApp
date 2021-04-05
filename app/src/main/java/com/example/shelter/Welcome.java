package com.example.shelter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class Welcome extends AppCompatActivity {
    ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    List<screenItem> mList;
    TabLayout tabLayout;
    AppCompatImageButton btn_right;
    AppCompatImageButton btn_left;
    AppCompatButton btn_get_started;
    int position=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        if (restorePrefData()){
            startActivity(new Intent(Welcome.this,Register.class));
            finish();
        }

        screenPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tab_layout);
        btn_left=findViewById(R.id.btn_left);
        btn_right=findViewById(R.id.btn_right);
        btn_get_started=findViewById(R.id.btn_getStarted);


        mList=new ArrayList<>();


        mList.add(new screenItem("Add Pet","You can add any number of pets to your app from floating button in bottom right corner",R.drawable.ic_pets));
        mList.add(new screenItem("Edit Pet Details","You can edit edit any information for any of your pets as name, gender and weight....",R.drawable.ic_edit));
        mList.add(new screenItem("View Your Profile","Also you can view your profile page and see your photo, name and account. and verify your account is added  ",R.drawable.ic_man));
        introViewPagerAdapter=new IntroViewPagerAdapter(this,mList);
        screenPager.setAdapter(introViewPagerAdapter);

        tabLayout.setupWithViewPager(screenPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==mList.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btn_right.setOnClickListener(v -> {
            position=screenPager.getCurrentItem();
            if (position<mList.size()){
                position++;
                screenPager.setCurrentItem(position);
            }
            if (position==mList.size()-1){
                loadLastScreen();
            }
        });

        btn_left.setOnClickListener(v -> {
            position=screenPager.getCurrentItem();
            if (position>0){
                position--;
                screenPager.setCurrentItem(position);
            }
        });

        btn_get_started.setOnClickListener(v -> {
            startActivity(new Intent(Welcome.this,Register.class));

            savePrefData();
        });

    }

    private boolean restorePrefData() {
        SharedPreferences pref=getApplicationContext().getSharedPreferences("myPref",MODE_PRIVATE);
        Boolean isOpened=pref.getBoolean("isIntroOpened",false);
        return isOpened;
    }

    private void savePrefData() {
        SharedPreferences pref=getApplicationContext().getSharedPreferences("myPref",MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.putBoolean("isIntroOpened",true);
        editor.apply();
    }

    private void loadLastScreen() {
        btn_right.setVisibility(View.INVISIBLE);
        btn_left.setVisibility(View.INVISIBLE);
        tabLayout.setVisibility(View.INVISIBLE);
        btn_get_started.setVisibility(View.VISIBLE);
    }
}