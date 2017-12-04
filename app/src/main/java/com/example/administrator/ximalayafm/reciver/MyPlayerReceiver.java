package com.example.administrator.ximalayafm.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

/**
 * Created by le.xin on 2017/3/23.
 */

public class MyPlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("MyPlayerReceiver.onReceive");
        XmPlayerManager.release();
    }
}
