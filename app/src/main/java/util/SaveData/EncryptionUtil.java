package util.SaveData;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class EncryptionUtil {

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("test.properties", 0);
    }

    /**
     * 安全存储数据
     * @param context
     * @param key
     * @param value
     */
    public static void saveData(Context context,String key,String value){
        if(TextUtils.isEmpty(value))return;
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, SafeStorageUtil.encrypt(context, value));
        editor.commit();
    }
    /**
     * 获取数据
     * @param context
     * @param key
     * @return
     */
    public static String getData(Context context, String key){
        SharedPreferences sp = getSharedPreferences(context);
        String tem = sp.getString(key, "");
        if(!TextUtils.isEmpty(tem)){
            tem = SafeStorageUtil.decrypt(context, tem);
        }
        return tem;
    }
}
