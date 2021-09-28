package common;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class BaseJsonBodyInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {


        Response response;
        Request originRequest = chain.request();
        Request.Builder newRequest = originRequest.newBuilder();
        RequestBody body = originRequest.body();

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        Charset charset = Charset.forName("UTF-8");
        MediaType contentType = body.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
            if (charset != null) {
                //读取原请求参数内容
                String requestParams = buffer.readString(charset);
                try {
                    //重新拼凑请求体
                    JSONObject jsonObject = new JSONObject(requestParams);

                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    String date = sDateFormat.format(new java.util.Date());

                    jsonObject.put("reqClientTime",date);
                    jsonObject.put("appId","com.demo.facerecognition");
                    jsonObject.put("id","");

                   // for (Map.Entry<String, String> entry : getCommonParams().entrySet()) {
                     //   jsonObject.put(entry.getKey(), entry.getValue());
                  //  }
                    RequestBody newBody = RequestBody.create(body.contentType(), jsonObject.toString());
                    newRequest.post(newBody);

                    String logStr = jsonObject.toString();

                    Log.d("RequestBody", "请求报文：" + logStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        response = chain.proceed(newRequest.build());

        return response;
    }

    private Map<String, ArrayList> getCommonParams() {

        Map<String, ArrayList> map = new HashMap<String, ArrayList>();

        return map;
    }
}
