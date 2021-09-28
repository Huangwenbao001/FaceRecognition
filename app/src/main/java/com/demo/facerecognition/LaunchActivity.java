package com.demo.facerecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LaunchActivity extends BaseActivity {


    private final int SPLASH_DISPLAY_LENGHT = 2000;//两秒后进入系统，时间可自行调整

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LaunchActivity.this,MainActivity.class);
                LaunchActivity.this.startActivity(intent);
                LaunchActivity.this.finish();
            }
        },SPLASH_DISPLAY_LENGHT);


    }
}