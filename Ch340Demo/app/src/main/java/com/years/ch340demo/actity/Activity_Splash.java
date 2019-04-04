package com.years.ch340demo.actity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.years.ch340demo.R;

/**
 * 作者：Created by Years on 2019/4/3.
 * 邮箱：791276337@qq.com
 */

public class Activity_Splash extends AppCompatActivity {
    private Handler handler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
    }

    private void initView() {
        handler= new Handler();
        handler.postDelayed(runnable, 1000);
    }
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            Intent intent=new Intent(Activity_Splash.this,Activity_Home.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK)
            handler.removeCallbacks(runnable);
        return super.onKeyDown(keyCode, event);
    }

}
