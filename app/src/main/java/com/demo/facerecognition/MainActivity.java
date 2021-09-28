package com.demo.facerecognition;

import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import common.BodyInterceptor;
import common.LoadPress;
import gjson.LoginData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.HttpUtil;
import util.SaveData.EncryptionUtil;

import static util.SaveData.EncryptionUtil.getData;

public class MainActivity extends BaseActivity {


    private ImageView bingPicImg;
    private EditText accountText;
    private EditText passwordText;
    private LoadPress loadPress;
    private CheckBox rememberPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUis();


    }


    // 初始化UI
    private void initUis () {
        // 初始化loading
        loadPress = new LoadPress();
        // 初始化背景图
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        loadBingPic();

        accountText = (EditText) findViewById(R.id.account_editText);
        passwordText = (EditText) findViewById(R.id.password_editText);

        // 记住密码
        rememberPassword = (CheckBox) findViewById(R.id.remember_pass);
        String isRemember = EncryptionUtil.getData(this,"remember_password");

        if (isRemember.equals("1")) {
            rememberPassword.setChecked(true);
            accountText.setText(EncryptionUtil.getData(this, "account"));
            passwordText.setText(EncryptionUtil.getData(this, "password"));
        }


        // 登录按钮操作
        Button loginBtn = (Button) findViewById(R.id.login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLogin();

                setRememberPd();
               // showAlert();
            }
        });

        Button forgetPwdBtn = (Button) findViewById(R.id.forget_button);
        forgetPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, ForgetPwdActivity.class);
                 startActivity(intent);
            }
        });
    }


    private void loadBingPic () {

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendGetOkHttpRequest(requestBingPic, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    private void setRememberPd () {
        if (rememberPassword.isChecked()) {
            EncryptionUtil.saveData(this,"remember_password","1");
            EncryptionUtil.saveData(this,"account",accountText.getText().toString());
            EncryptionUtil.saveData(this,"password",passwordText.getText().toString());

        } else {

            EncryptionUtil.saveData(this,"remember_password","0");
        }
    }

    // 请求登录
    private void gotoLogin () {

        String account = accountText.getText().toString();
        String password = passwordText.getText().toString();


        EncryptionUtil.saveData(this,"account",accountText.getText().toString());

        if (account.length() < 1) {

            Toast.makeText(MainActivity.this,"请输入账号",Toast.LENGTH_SHORT).show();
            return;
        } else if (password.length() < 1) {
            Toast.makeText(MainActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
            return;
        }


        loadPress.showProgressDialog(MainActivity.this);

        String url = "http://47.108.116.107:8006/hbp/uc/usercenter/auth/login.do";

        Map map = new HashMap();
        map.put("identifier", account);
        map.put("credential", password);
        map.put("authType", "1");

        HttpUtil.sendPostJSONOkHttpRequest(url, map, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();

                Gson gson = new Gson();
                final LoginData loginData = gson.fromJson(responseText, LoginData.class);
                String code = loginData.code;
                String userId = loginData.data.userId;

                // 保存userId
                EncryptionUtil.saveData(MainActivity.this,"userId",userId);

                Looper.prepare();
                loadPress.closeProgressDialog();
                if (code.equals("200")) {
                    if (loginData.data.level.equals("0")) {
                        showAlert();
                    } else {
                        Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this,loginData.msg,Toast.LENGTH_SHORT).show();
                }
                Looper.loop();


            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                loadPress.closeProgressDialog();
                Looper.loop();
            }
        });

    }

    private void showAlert (){
        String account = accountText.getText().toString();

        EncryptionUtil.saveData(this,"account",account);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("实名认证")
                .setMessage("尊敬的用户您还未做实名认证，请先做实名认证！")
                .setPositiveButton("实名认证", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoCertification();
                    }
                })
                .setNegativeButton("暂时不做", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        alertDialog.show();
    }

    private void gotoCertification() {
        Intent intent = new Intent(MainActivity.this, CertificationActivity.class);
        startActivity(intent);
    }

}