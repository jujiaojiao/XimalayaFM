package com.example.administrator.ximalayafm.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2017/12/18.
 */

public class MyRecevier extends BroadcastReceiver {
    public static final String TAG = MyRecevier.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case "com.app.ximalaya.recevie":
                String ggg = intent.getStringExtra("ggg");
                EventBus.getDefault().post(ggg);
                Log.e(TAG, "onReceive: 广播接受成功 :  "+ggg);
                break;
        }
    }

}
