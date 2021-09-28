package util.SaveData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class SafeStorageUtil {
    private static String AESKEY;

    /**
     * 获得应用程序包名
     * @param context
     * @return
     */
    private static String getPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * 设备IMEI号
     * @param mContext
     * @return
     */
    public static String getDeviceID(Context mContext) {
        StringBuilder stringBuilder = new StringBuilder();
        String imei = getIMEI(mContext);
        return imei;
    }

    @SuppressLint("MissingPermission")
    private static String getIMEI(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取唯一标识
     * @param mContext
     * @return
     */
    private static String getAppUnique(Context mContext){
        return getDeviceID(mContext)+getPackageName(mContext);
    }

    private static String getAESKey(Context context) {
        if (!TextUtils.isEmpty(AESKEY))return AESKEY;
        //取16位作为密钥key
        AESKEY = HashUtils.getHash(getAppUnique(context), "MD5").substring(0, 16);
        return AESKEY;
    }

    public static String encrypt(Context context, String data){
        return SafeAESTool.encrypt(getAESKey(context), data);
    }

    public static String decrypt(Context context,String data){
        return SafeAESTool.decrypt(getAESKey(context), data);
    }
}
