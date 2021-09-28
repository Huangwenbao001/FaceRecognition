package com.demo.facerecognition;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import common.LoadPress;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.HttpUtil;


public class ForgetPwdActivity extends BaseSwipeActivity {

    private Button faceBtn1;
    private Button faceBtn2;
    private String ImageA;
    private String ImageB;
    private String Timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);

        TextView titleView = (TextView) findViewById(R.id.title_textView);
        titleView.setText("人脸比对");

        faceBtn1 = (Button) findViewById(R.id.face_button1);
        faceBtn2 = (Button) findViewById(R.id.face_button2);

        try {
            Timestamp = dateToStamp();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        faceBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开照相机拍照选取
                ActivityCompat.requestPermissions(ForgetPwdActivity.this,new String[] {
                        Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                },1);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        });
        faceBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开相册选取
              //  Intent it = new Intent();
             //   it.setType("image/*");
              //  it.setAction(Intent.ACTION_GET_CONTENT);
              //  startActivityForResult(it, 2);

                //打开照相机拍照选取
                ActivityCompat.requestPermissions(ForgetPwdActivity.this,new String[] {
                        Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                },1);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        });

        Button comfirmBtn = (Button) findViewById(R.id.confirm_button);
        comfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoIai();
            }
        });

        scale = getResources().getDisplayMetrics().density;
    }
    float scale;
    //这个方法我只知道是为了适应不同屏幕的显示,具体还真不理解,先用着吧
    public int getPixel(int old) {
        return (int) (old * scale + 0.5f);
    }


    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp() throws ParseException {

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String s = sDateFormat.format(new java.util.Date());

        String res;
        Date date = sDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    //由于Intent使用的是 startActivityForResult,调用此回调方法接受返回数据,即照片。

    private void gotoIai () {
        String url = "https://iai.tencentcloudapi.com";

        LoadPress loadPress = new LoadPress();
        loadPress.showProgressDialog(ForgetPwdActivity.this);

        FormBody body = new FormBody.Builder()
                .add("ImageA",ImageA)
                .add("ImageB",ImageB)
                .add("FaceModelVersion","3.0")
                .add("QualityControl","1")
                .add("NeedRotateDetection","0")
                .add("Version","2020-03-03")
                .add("Region","ap-guangzhou")
                .add("Action","CompareFace")
                .add("Timestamp",Timestamp)
                .build();

        HttpUtil.sendPostOkHttpRequest(url,body, new okhttp3.Callback () {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d("For",responseText);
                Looper.prepare();
                loadPress.closeProgressDialog();
                Looper.loop();
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                Log.d("For","网络错误");
                Looper.prepare();
                loadPress.closeProgressDialog();
                Looper.loop();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Uri mImageCaptureUri = data.getData();
            if (mImageCaptureUri == null && data != null) {
                if (data.hasExtra("data")) {
                    Bitmap bitmap = data.getParcelableExtra("data");
                    Drawable drawable = new BitmapDrawable(getResources(),bitmap);

                    if (ImageA == null) {
                        faceBtn1.setBackground(drawable);
                        ImageA = bitmapToBase64(bitmap);
                        return;
                    } else {
                        faceBtn2.setBackground(drawable);
                        ImageB = bitmapToBase64(bitmap);
                        return;
                    }


                }
            }
            try {
                // 这个方法是根据uri获取Bitmap图片的静态方法
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                // 获取屏幕分辨率
                DisplayMetrics dm_2 = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm_2);
                // 图片分辨率与屏幕分辨比例
                float scale_2 = bitmap.getWidth() / (float) dm_2.widthPixels;
                Bitmap newBitMap = null;

                if (scale_2 > 1) {
                    newBitMap = zoomBitmap(bitmap,bitmap.getWidth() / scale_2, bitmap.getHeight() / scale_2);
                    bitmap.recycle();
                }

                if (newBitMap != null) {

                    ImageB = bitmapToBase64(newBitMap);
                    Drawable drawable = new BitmapDrawable(getResources(),newBitMap);
                    faceBtn2.setBackground(drawable);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bundle extras = data.getExtras();
            if (extras != null) {
                //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                Bitmap image = extras.getParcelable("data");
                if (image != null) {

                }
            }
        }
    }

    // 对分辨率较大的图片进行缩放
    public Bitmap zoomBitmap(Bitmap bitmap, float width, float height) {

        int w = bitmap.getWidth();

        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();

        float scaleWidth = ((float) width / w);

        float scaleHeight = ((float) height / h);

        matrix.postScale(scaleWidth, scaleHeight);// 利用矩阵进行缩放不会造成内存溢出

        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

        return newbmp;

    }

    /*
     * bitmap转base64
     * */
    private static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String imageToBase64(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data,Base64.NO_CLOSE);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

}