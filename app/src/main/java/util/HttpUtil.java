package util;

import android.os.Looper;
import android.util.Log;

import com.webank.mbank.okio.Buffer;
import com.webank.mbank.okio.BufferedSink;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import common.BaseJsonBodyInterceptor;
import common.BodyInterceptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    public static void sendGetOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendPostOkHttpRequest(String url, RequestBody formBody, okhttp3.Callback callback) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                okHttpClient.newCall(request).enqueue(callback);
            }
        });

    }

    public static void sendPostJSONOkHttpRequest(String url, Map map, okhttp3.Callback callback) {

        JSONObject jsonObject = new JSONObject(map);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        okhttp3.Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new BaseJsonBodyInterceptor())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                okHttpClient.newCall(request).enqueue(callback);
           }
        });
    }

}
