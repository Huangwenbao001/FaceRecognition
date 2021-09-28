package com.demo.facerecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.demo.facerecognition.BaseSwipeActivity;
import com.demo.facerecognition.R;

public class SuccessActivity extends BaseSwipeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        TextView titleView = (TextView) findViewById(R.id.title_textView);
        titleView.setText("认证成功");

        TextView contentView = (TextView) findViewById(R.id.textview);
        Spannable string = new SpannableString("认证成功！\n\n恭喜您！\n您已完成实名认证，可返回查看。");
        string.setSpan(new StyleSpan(Typeface.BOLD),0,5,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        string.setSpan(new AbsoluteSizeSpan(60),0,5,Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        contentView.setText(string);


        Button confirmBtn = (Button) findViewById(R.id.confirm_button);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回登录，并且发广播销毁所有的活动
                Intent intent = new Intent("com.example.broadcastbestpractice.FORCE_OFFLINE");
                sendBroadcast(intent);
            }
        });
    }
}
