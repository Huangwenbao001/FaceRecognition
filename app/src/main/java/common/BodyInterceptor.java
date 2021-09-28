package common;

import android.os.Build;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.SaveData.SafeStorageUtil;

public class BodyInterceptor implements Interceptor {
    private Object Request;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        request = handleRequest(request);

        if (request == null) {
            throw new RuntimeException("Request返回值不能为空");
        }
        return chain.proceed(request);
    }

    /**
     * 解析请求参数
     */
    public static Map<String, String> parseParams(Request request) {
        //GET POST DELETE PUT PATCH
        String method = request.method();
        Map<String, String> params = null;
        if ("GET".equals(method)) {
            params = doGet(request);
        } else if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
            RequestBody body = request.body();
            if (body != null && body instanceof FormBody) {
                params = doForm(request);
            }
        }
        return params;
    }

    /**
     * 获取get方式的请求参数
     * @param request
     * @return
     */
    private static Map<String, String> doGet(Request request) {
        Map<String, String> params = null;
        HttpUrl url = request.url();
        Set<String> strings = url.queryParameterNames();
        if (strings != null) {
            Iterator<String> iterator = strings.iterator();
            params = new HashMap<>();
            int i = 0;
            while (iterator.hasNext()) {
                String name = iterator.next();
                String value = url.queryParameterValue(i);
                params.put(name, value);
                i++;
            }
        }
        return params;
    }

    /**
     * 获取表单的请求参数
     * @param request
     * @return
     */
    private static Map<String, String> doForm(Request request) {
        Map<String, String> params = null;
        FormBody body = null;
        try {
            body = (FormBody) request.body();
        } catch (ClassCastException c) {
        }
        if (body != null) {
            int size = body.size();
            if (size > 0) {
                params = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    params.put(body.name(i), body.value(i));
                }
            }
        }
        return params;
    }

    /**
     * 重构Request增加公共参数
     *
     */
    public Request handleRequest(Request request) {
        Map<String, String> params = parseParams(request);
        if (params == null) {
            params = new HashMap<>();
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        String date = sDateFormat.format(new java.util.Date());

        String deviceId = "com.demo.facerecognition";

        params.put("reqClientTime",date);
        params.put("appId",deviceId);
        params.put("id","");

        String method = request.method();

        if ("POST".equals(method)) {
            if (request.body() instanceof FormBody) {
                if (request.body() instanceof FormBody) {
                    FormBody.Builder bodyBuilder = new FormBody.Builder();
                    Iterator<Map.Entry<String, String>> entryIterator = params.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        String key = entryIterator.next().getKey();
                        String value = entryIterator.next().getValue();
                        bodyBuilder.add(key, value);
                    }
                    return request.newBuilder().method(method, bodyBuilder.build()).build();
                }
            }
        }

        return request;
    }


}
