package com.demo.facerecognition;

import android.os.Bundle;

import android.content.Intent;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.common.hash.Hashing;
import com.webank.facelight.api.WbCloudFaceContant;
import com.webank.facelight.process.FaceVerifyStatus;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import common.AppHandler;
import common.GetFaceId;
import common.LoadPress;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.HttpUtil;


import com.webank.facelight.api.WbCloudFaceVerifySdk;
import com.webank.facelight.api.listeners.WbCloudFaceVerifyLoginListener;
import com.webank.facelight.api.listeners.WbCloudFaceVerifyResultListener;
import com.webank.facelight.api.result.WbFaceError;
import com.webank.facelight.api.result.WbFaceVerifyResult;
import com.webank.mbank.wehttp2.WeLog;
import com.webank.mbank.wehttp2.WeOkHttp;
import com.webank.mbank.wehttp2.WeReq;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kotlin.text.Charsets;
import util.SaveData.EncryptionUtil;

public class CertificationActivity extends BaseSwipeActivity {
    private static final String TAG = "CertificationActivity";
    private boolean isShowSuccess;
    private boolean isShowFail;
    private boolean isRecordVideo;
    private boolean isPlayVoice;
    private String color;

    private AppHandler appHandler;


    private String name;
    private String id;

    private EditText nameEditText;
    private EditText idCardEditText;
    private LoadPress loadPress;

    // 正式appId：IDAW7qeE   secret：7fLMVnUNCieByWvaT0HmGUBBI2nnDYdqvRa2ZQRHSrPir7Xx9pQ5AUZpdapYU1ge
    // 测试appId：TIDAWkO5   secret：ohWbHiYnRc3E9nAiMQCxBtiNWLGN0lzM0RShSJYSSqLtAeS3vwxqaHUFYZ3zfXiO

    private String orderNo = "JSTULldj" + getNum(9999999);
    private String appId = "IDAW7qeE";
    //此处为demo模拟，请输入标识唯一用户的userId
    private String userId = "LKFJD1003";
    //此处为demo模拟，请输入32位随机数
    private String nonce = "52014832029547845621032584566846";
    //此处为demo使用，由合作方提供包名申请，统一下发
    private String secret = "7fLMVnUNCieByWvaT0HmGUBBI2nnDYdqvRa2ZQRHSrPir7Xx9pQ5AUZpdapYU1ge";
    //此处为demo使用，由合作方提供包名申请，统一下发
    private String keyLicence = "LQiifE7SnxGjorPlAv5rme1lMLM+oh6YIzx/O+JIUsiw1aTma568ZVv+3ijTmz1YGKUwF+AspUFFp8TijVry5AfNDhl8eeYoLigmBTeJsrCKzZcqCBDNxCpXbAyGyunB5VkTBWepwUVTD3EdxFaQbSYYTrquEWpveNqQbmh7mlOEZdFCaHpOHNETIZBv4h+1qrVnZqCF5fHISg5e3xNrvttgKnYtfGITayMOnBvzOneFAZeBrGY72CiUxgMfNBV9GOtWL3AjIrQMRvLkaPVpE+x0Ma7gf6Rr+s2h+OQbKhnlS1muWpD7yMEKs2ov1M6MZhxclaq6cAFfNsJlpmEGqw==";

    private String compareType;

    private String access_token;
    private String sign_ticket;
    private String nonce_ticket;
    private String sign;
    private String nonce_sign;
    private String faceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certification);

        appHandler = new AppHandler(CertificationActivity.this);

        // 初始化请求
        initHttp();
        setListeners();

        String account = EncryptionUtil.getData(this,"account");
        Log.d(TAG,account);
    }


    // 初始化各部件 ***************************************************
    private WeOkHttp myOkHttp = new WeOkHttp();
    // 初始化各部件
    private void initHttp() {

        myOkHttp = new WeOkHttp();
        //拿到OkHttp的配置对象进行配置
        //WeHttp封装的配置
        myOkHttp.config()
                //配置超时,单位:s
                .timeout(20, 20, 20)
                //添加PIN
                .log(WeLog.Level.BODY);


        // 初始化loading
        loadPress = new LoadPress();

        // 隐藏记住密码
        LinearLayout view = (LinearLayout) findViewById(R.id.remember_view);
        view.setVisibility(View.GONE);
        Button forgetBtn = (Button) findViewById(R.id.forget_button);
        forgetBtn.setVisibility(View.GONE);


        nameEditText = (EditText) findViewById(R.id.account_editText);
        idCardEditText = (EditText) findViewById(R.id.password_editText);
        idCardEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        nameEditText.setHint("请输入姓名");
        idCardEditText.setHint("请输入身份证号码");

       // nameEditText.setText("黄文豹");
     //  idCardEditText.setText("460028198910180839");

        ImageView nameImage = (ImageView) findViewById(R.id.icon_account);
        ImageView idCardImage = (ImageView) findViewById(R.id.icon_password);

        nameImage.setBackgroundResource(R.drawable.name);
        idCardImage.setBackgroundResource(R.drawable.idcard_icon);


        Button button = (Button) findViewById(R.id.login_button);
        button.setText("认证");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  Intent intent = new Intent(CertificationActivity.this, SuccessActivity.class);
               // startActivity(intent);
              //  CertificationActivity.this.finish();
                // tokenFromTXService();
                checkOnId(AppHandler.DATA_MODE_DESENSE);

            }
        });
    }

    private void setListeners() {

        //默认选择白色模式
        color = WbCloudFaceContant.WHITE;
        //默认不展示成功/失败页面
        isShowSuccess = false;
        isShowFail = true;
        //默认不录制视频
        isRecordVideo = true;
        //默认不播放提示语
        isPlayVoice = true;
        //设置选择的比对类型  默认为公安网纹图片对比
        //公安网纹图片比对 WbCloudFaceContant.ID_CRAD
        //仅活体检测  WbCloudFaceContant.NONE
        //默认公安网纹图片对比
        compareType = WbCloudFaceContant.ID_CARD;
        // signUseCase.execute(appHandler.DATA_MODE_DESENSE, appId, userId, nonce);

    }

    // 获取缓存的账号
    private String getAccount () {
        return EncryptionUtil.getData(this,"account");
    }

    // 检查姓名跟身份证 *****************************************
    private void checkOnId(String mode) {
        if (compareType.equals(WbCloudFaceContant.ID_CARD)) {
            name = nameEditText.getText().toString().trim();
            id = idCardEditText.getText().toString().trim();
            if (name != null && name.length() != 0) {
                if (id != null && id.length() != 0) {
                    if (id.contains("x")) {
                        id = id.replace('x','X');
                    }
                    IdentifyCardValidate vali = new IdentifyCardValidate();
                    String msg = vali.validate_effective(id);
                    if (msg.equals(id)) {
                        Log.i(TAG,"Param right!");
                        Log.i(TAG,"Called face verify sdk MODE="+mode);

                        // 获取token
                        tokenFromTXService();


                    } else {
                        Toast.makeText(CertificationActivity.this, "用户证件错误",Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    Toast.makeText(CertificationActivity.this,"用户证件不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(CertificationActivity.this,"用户姓名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    // 获取faceId
    //特别注意：此方法仅供demo使用，开发时需要自己的后台提供接口获得faceId
    public void getFaceId(final FaceVerifyStatus.Mode mode, final String sign) {
        Log.d(TAG, "start getFaceId");

        final String order = "testReflect" + System.currentTimeMillis();
        //此处为demo使用体验，实际生产请使用控制台给您分配的appid


        if (compareType.equals(WbCloudFaceContant.NONE)) {
            Log.d(TAG, "仅活体检测不需要faceId，直接拉起sdk");
            openCloudFaceService(mode, appId, order, sign,"");
            return;
        }


       // 基础版 String url = "https://miniprogram-kyc.tencentcloudapi.com/api/server/getFaceId";
        String url = "https://miniprogram-kyc.tencentcloudapi.com/api/server/getAdvFaceId?";

        GetFaceId.GetFaceIdParam param = new GetFaceId.GetFaceIdParam();
        param.orderNo = orderNo;
        param.webankAppId = appId;
        param.version = "1.0.0";
        param.userId = userId;
        param.sign = sign;
        param.nonce = nonce;

        if (compareType.equals(WbCloudFaceContant.ID_CARD)) {
            param.name = name;
            param.idNo = id;
        }

        GetFaceId.requestExec(myOkHttp, url, param, new WeReq.Callback<GetFaceId.GetFaceIdResponse>() {
            @Override
            public void onStart(WeReq weReq) {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onFailed(WeReq weReq, WeReq.ErrType errType, int i, String s, IOException e) {
                Looper.prepare();
                loadPress.closeProgressDialog();
                Toast.makeText(CertificationActivity.this,"登录异常(faceId请求失败：code=)" + i + "message" + s, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onSuccess(WeReq weReq, GetFaceId.GetFaceIdResponse getFaceIdResponse) {
                if (getFaceIdResponse != null) {
                    String code = getFaceIdResponse.code;
                    if (code.equals("0")) {
                        GetFaceId.Result result = getFaceIdResponse.result;
                        if (result != null) {

                            faceId = result.faceId;
                            if (!TextUtils.isEmpty(faceId)) {
                                Log.d(TAG, "faceId请求成功：" + faceId);
                                openCloudFaceService(mode, appId, orderNo, sign, faceId);

                            } else {
                                loadPress.closeProgressDialog();
                                Log.d(TAG,"faceId为空");
                                Toast.makeText(CertificationActivity.this,"登录异常",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loadPress.closeProgressDialog();
                            Log.e(TAG, "faceId请求失败:getFaceIdResponse result is null.");
                            Toast.makeText(CertificationActivity.this, "登录异常(faceId请求失败:getFaceIdResponse result is null)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loadPress.closeProgressDialog();
                        Log.e(TAG, "faceId请求失败:code=" + code + "msg=" + getFaceIdResponse.msg);
                        Toast.makeText(CertificationActivity.this, "登录异常(faceId请求失败:code=" + code + "msg=" + getFaceIdResponse.msg + ")", Toast.LENGTH_SHORT).show();
                    }
                }  else {
                    loadPress.closeProgressDialog();
                    Log.e(TAG, "faceId请求失败:getFaceIdResponse is null.");
                    Toast.makeText(CertificationActivity.this, "登录异常(faceId请求失败:getFaceIdResponse is null)", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    // 从腾讯服务器获取各数据  ****************************************
    // 获取token
    public void tokenFromTXService () {
        // https://miniprogram-kyc.tencentcloudapi.com/api/oauth2/access_token?app_id=xxx&secret=xxx&grant_type=client_credential&version=1.0.0

        loadPress.showProgressDialog(CertificationActivity.this);

        String url = "https://miniprogram-kyc.tencentcloudapi.com/api/oauth2/access_token?app_id=" + appId + "&secret=" + secret + "&grant_type=client_credential&version=1.0.0";

        HttpUtil.sendGetOkHttpRequest(url, new okhttp3.Callback(){
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                loadPress.closeProgressDialog();
                Log.d("TXTest",e.getMessage());
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String responseText = response.body().string();
                JSONObject object = null;

                try {
                    object = new JSONObject(responseText);
                    access_token = object.getString("access_token");

                    Log.d(TAG,"获取Token成功：" + access_token);

                    // 获取ticket
                    ticketFromService();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    // 获取sign_ticket
    public void ticketFromService () {
        //https://miniprogram-kyc.tencentcloudapi.com/api/oauth2/api_ticket?app_id=xxx&access_token=xxx&type=SIGN&version=1.0.0
        String url = "https://miniprogram-kyc.tencentcloudapi.com/api/oauth2/api_ticket?app_id=" + appId + "&access_token=" + access_token + "&type=SIGN&version=1.0.0";
        HttpUtil.sendGetOkHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                loadPress.closeProgressDialog();
                Log.d("TXTest",e.getMessage());
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String responseText = response.body().string();
                JSONObject object = null;

                try {
                    object = new JSONObject(responseText);

                    JSONArray jsonArray = object.getJSONArray("tickets");
                    JSONObject value = jsonArray.getJSONObject(0);

                    sign_ticket = value.getString("value");

                    // 签名
                    List<String> stringList = new ArrayList();
                    stringList.add(appId);
                    stringList.add(userId);
                    stringList.add(nonce);
                    stringList.add("1.0.0");
                    sign = sign(stringList,sign_ticket);

                    Log.d(TAG,"获取获取sign_ticket成功：" + sign_ticket);

                    // 获取ticket
                    nonce_ticketFromService();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    // 获取nonce_ticket
    public void nonce_ticketFromService () {
        //https://miniprogram-kyc.tencentcloudapi.com/api/oauth2/api_ticket?app_id=xxx&access_token=xxx&type=NONCE&version=1.0.0&user_id=xxx
        String url = "https://miniprogram-kyc.tencentcloudapi.com/api/oauth2/api_ticket?app_id=" + appId + "&access_token=" + access_token + "&type=NONCE&version=1.0.0" + "&user_id=" + userId;
        HttpUtil.sendGetOkHttpRequest(url, new okhttp3.Callback(){
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                loadPress.closeProgressDialog();
                Log.d("TXTest",e.getMessage());
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String responseText = response.body().string();
                JSONObject object = null;

                try {

                    object = new JSONObject(responseText);

                    JSONArray jsonArray = object.getJSONArray("tickets");
                    JSONObject value = jsonArray.getJSONObject(0);

                    nonce_ticket = value.getString("value");

                    Log.d(TAG,"获取获取nonce_ticket成功：" + nonce_ticket);
                    Log.d(TAG,"获取nonce_ticket的报文：" + responseText);

                    // 签名
                    String temp_sign = appId + ",  " + userId + ",  " + nonce + ",  " + "1.0.0" + ",  " + sign_ticket;

                    String testSign = "appId:" + appId + ",  " + "userId:" + userId + ",  " + "nonce:" + nonce + ",  " + "version:" + "1.0.0" + ",  " + "nonce_ticket:" + nonce_sign;
                    //  sign = getSHA(temp_sign);

                    // 签名
                    List<String> stringList1 = new ArrayList();
                    stringList1.add(appId);
                    stringList1.add(userId);
                    stringList1.add(nonce);
                    stringList1.add("1.0.0");
                    nonce_sign = sign(stringList1,nonce_ticket);

                    Log.d(TAG,"SIGN签名的参数：" + temp_sign);
                    Log.d(TAG,"nonce_sign签名的参数：" + temp_sign);
                    Log.d(TAG,"获取获取sign成功：" + sign + "nonce_sign:"+ nonce_sign);

                    //  Looper.prepare();

                    //    openCloudFaceService(FaceVerifyStatus.Mode.GRADE, appId, orderNo, sign, "");
                    //  closeProgressDialog();
                    //   Looper.loop();
                    // 获取faceId
                    getFaceId(FaceVerifyStatus.Mode.GRADE,sign);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    // 拉起刷脸
    public void openCloudFaceService(FaceVerifyStatus.Mode mode, String appId, String order, String sign,String faceId) {

        Log.d(TAG, "openCloudFaceService");
        Bundle data = new Bundle();

        if (faceId.length()< 1) {
            WbCloudFaceVerifySdk.InputData inputData = new WbCloudFaceVerifySdk.InputData(
                    order,
                    appId,
                    "1.0.0",
                    nonce,
                    userId,
                    nonce_sign,
                    mode,
                    keyLicence);
            data.putSerializable(WbCloudFaceContant.INPUT_DATA, inputData);
            //公安网纹图片比对 WbCloudFaceContant.ID_CRAD
            //仅活体检测  WbCloudFaceContant.NONE
            //默认公安网纹图片比对
            data.putString(WbCloudFaceContant.COMPARE_TYPE, WbCloudFaceContant.NONE);

        } else {
            WbCloudFaceVerifySdk.InputData inputData = new WbCloudFaceVerifySdk.InputData(
                    faceId,
                    order,
                    appId,
                    "1.0.0",
                    nonce,
                    userId,
                    nonce_sign,
                    mode,
                    keyLicence);
            data.putSerializable(WbCloudFaceContant.INPUT_DATA, inputData);
            //公安网纹图片比对 WbCloudFaceContant.ID_CRAD
            //仅活体检测  WbCloudFaceContant.NONE
            //默认公安网纹图片比对
            data.putString(WbCloudFaceContant.COMPARE_TYPE, WbCloudFaceContant.ID_CARD);
        }

        //是否展示刷脸成功页面，默认不展示
        data.putBoolean(WbCloudFaceContant.SHOW_SUCCESS_PAGE, isShowSuccess);
        //是否展示刷脸失败页面，默认不展示
        data.putBoolean(WbCloudFaceContant.SHOW_FAIL_PAGE, isShowFail);
        //颜色设置,sdk内置黑色和白色两种模式，默认白色
        //如果客户想定制自己的皮肤，可以传入WbCloudFaceContant.CUSTOM模式,此时可以配置ui里各种元素的色值
        //定制详情参考app/res/colors.xml文件里各个参数
        data.putString(WbCloudFaceContant.COLOR_MODE, color);
        //是否需要录制上传视频 默认不需要
        data.putBoolean(WbCloudFaceContant.VIDEO_UPLOAD, isRecordVideo);
        //是否播放提示音，默认不播放
        data.putBoolean(WbCloudFaceContant.PLAY_VOICE, isPlayVoice);
        //识别阶段合作方定制提示语,可不传，此处为demo演示
        data.putString(WbCloudFaceContant.CUSTOMER_TIPS_LIVE, "仅供体验使用 请勿用于投产!");
        //上传阶段合作方定制提示语,可不传，此处为demo演示
        data.putString(WbCloudFaceContant.CUSTOMER_TIPS_UPLOAD, "仅供体验使用 请勿用于投产!");
        //合作方长定制提示语，可不传，此处为demo演示
        //如果需要展示长提示语，需要邮件申请
        data.putString(WbCloudFaceContant.CUSTOMER_LONG_TIP, "本demo提供的appId仅用于体验，实际生产请使用控制台给您分配的appId！");
        //设置选择的比对类型  默认为公安网纹图片对比
        //sdk log开关，默认关闭，debug调试sdk问题的时候可以打开
        //【特别注意】上线前请务必关闭sdk log开关！！！
//        data.putBoolean(WbCloudFaceContant.IS_ENABLE_LOG, false);

        Log.d(TAG, "WbCloudFaceVerifySdk initSdk");


        // 基础版initSdk
        WbCloudFaceVerifySdk.getInstance().initAdvSdk(CertificationActivity.this, data, new WbCloudFaceVerifyLoginListener() {
            @Override
            public void onLoginSuccess() {
                //登录sdk成功
                Log.i(TAG, "onLoginSuccess");
                loadPress.closeProgressDialog();
                //拉起刷脸页面
                WbCloudFaceVerifySdk.getInstance().startWbFaceVerifySdk(CertificationActivity.this, new WbCloudFaceVerifyResultListener() {
                    @Override
                    public void onFinish(WbFaceVerifyResult result) {
                        //得到刷脸结果
                        loadPress.closeProgressDialog();
                        if (result != null) {
                            if (result.isSuccess()) {
                                Log.d(TAG, "刷脸成功! Sign=" + result.getSign() + "; liveRate=" + result.getLiveRate() +
                                        "; similarity=" + result.getSimilarity() + "userImageString=" + result.getUserImageString());

                                loadPress.showProgressDialog(CertificationActivity.this);
                                sendToken();

                                if (!isShowSuccess) {
                                    Toast.makeText(CertificationActivity.this, "刷脸成功", Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                WbFaceError error = result.getError();
                                if (error != null) {
                                    Log.d(TAG, "刷脸失败！domain=" + error.getDomain() + " ;code= " + error.getCode()
                                            + " ;desc=" + error.getDesc() + ";reason=" + error.getReason());
                                    if (error.getDomain().equals(WbFaceError.WBFaceErrorDomainCompareServer)) {
                                        Log.d(TAG, "对比失败，liveRate=" + result.getLiveRate() +
                                                "; similarity=" + result.getSimilarity());
                                    }
                                    if (!isShowSuccess) {
                                        Toast.makeText(CertificationActivity.this, "刷脸失败!" + error.getDesc(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.e(TAG, "sdk返回error为空！");
                                }
                            }
                        } else {
                            Log.e(TAG, "sdk返回结果为空！");
                        }
                        //测试用代码
                        //不管刷脸成功失败，只要结束了，自带对比和活体检测都更新userId
                        if (!compareType.equals(WbCloudFaceContant.ID_CARD)) {
                            Log.d(TAG, "更新userId");
                            userId = "WbFaceVerifyREF" + System.currentTimeMillis();
                        }
                    }
                });
            }

            @Override
            public void onLoginFailed(WbFaceError error) {
                //登录失败
                loadPress.closeProgressDialog();
                Log.i(TAG, "onLoginFailed!");
                if (error != null) {
                    Log.d(TAG, "登录失败！domain=" + error.getDomain() + " ;code= " + error.getCode()
                            + " ;desc=" + error.getDesc() + ";reason=" + error.getReason());
                    if (error.getDomain().equals(WbFaceError.WBFaceErrorDomainParams)) {
                        Toast.makeText(CertificationActivity.this, "传入参数有误！" + error.getDesc(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CertificationActivity.this, "登录刷脸sdk失败！" + error.getDesc(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "sdk返回error为空！");
                }
            }
        });
    }

    private void sendToken () {

        loadPress.showProgressDialog(CertificationActivity.this);
        String url = "http://47.108.116.107:8006/hbp/uc/usercenter/user/updateUserLevel.do";

        String userId = EncryptionUtil.getData(this,"userId");

        Map map = new HashMap();
        map.put("userId", userId);
        map.put("name", name);
        map.put("identifyNum", id);
        map.put("level", "2");

        HttpUtil.sendPostJSONOkHttpRequest(url, map, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                loadPress.closeProgressDialog();
                Toast.makeText(CertificationActivity.this,"上送失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                Looper.prepare();
                Intent intent = new Intent(CertificationActivity.this, SuccessActivity.class);
                startActivity(intent);
                CertificationActivity.this.finish();
                loadPress.closeProgressDialog();
                Looper.loop();
            }
        });
    }

    // 随机数(订单号)
    public static String getNum(int endNum){

        if(endNum > 0){
            Random random = new Random();
            int num = random.nextInt(endNum) + 1000;
            String str = String.valueOf(num);
            return str;
        }
        return "0";
    }

    // 哈希签名
    public static String sign(List<String> values, String ticket) {
        if (values == null) {
            throw new NullPointerException("values is null");
        }
        values.removeAll(Collections.singleton(null));// remove null
        values.add(ticket);
        java.util.Collections.sort(values);
        StringBuilder sb = new StringBuilder();
        for (String s : values) {
            sb.append(s);
        }
        return Hashing.sha1().hashString(sb, Charsets.UTF_8).toString().toUpperCase();
    }

}