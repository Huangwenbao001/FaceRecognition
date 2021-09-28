package common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.demo.facerecognition.MainActivity;

public class ForceOfflineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("警告！");
        builder.setMessage("您的账号在别的地方登录，请重新登录");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ActivityCollector.finishAll(); // 销毁所有活动
                Intent i = new Intent(context, MainActivity.class);
                context.startActivity(i);
            }
        });
        builder.show();
    }
}
