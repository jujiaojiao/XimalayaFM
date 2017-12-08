package com.example.administrator.ximalayafm;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;

import com.example.administrator.ximalayafm.dao.DBUtil;
import com.example.administrator.ximalayafm.reciver.MyPlayerReceiver;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.player.appnotification.XmNotificationCreater;
import com.ximalaya.ting.android.opensdk.util.BaseUtil;
import com.ximalaya.ting.android.opensdk.util.Logger;
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager;
import com.ximalaya.ting.android.sdkdownloader.http.RequestParams;
import com.ximalaya.ting.android.sdkdownloader.http.app.RequestTracker;
import com.ximalaya.ting.android.sdkdownloader.http.request.UriRequest;

import org.xutils.x;


/**
 * Created by le.xin on 2016/6/12.
 */
public class TingApplication extends Application {

    private DBUtil instance;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
//        x.Ext.setDebug(true); // 是否输出debug日志, 开启debug会影响性能.

        String mp3 = getExternalFilesDir("mp3").getAbsolutePath();
        System.out.println("地址是  " + mp3);

        CommonRequest mXimalaya = CommonRequest.getInstanse();
        String mAppSecret;
//        if(!DTransferConstants.isRelease) {
//            mAppSecret = "c1c9c9cc4b2d3e2982ce09513dad8ac8";
//            mXimalaya.setAppkey("7721f10cb16aeb985303615f8e9f4aa5");
//            mXimalaya.setPackid("com.app.joke.android");
//        } else {
//            mAppSecret = "4d8e605fa7ed546c4bcb33dee1381179";
//            mXimalaya.setAppkey("b617866c20482d133d5de66fceb37da3");
////            mAppSecret = "4d8e605fa7ed546c4bcb33dee1381179";
////            mXimalaya.setAppkey("b617866c20482d133d5de66fceb37da3");
//            mXimalaya.setPackid("com.app.test.android");
//        }
        mXimalaya.setAppkey("d4df2a6e290d27e29bf1bedc06cf8daf");
        mXimalaya.setPackid("com.example.administrator.ximalayafm");
        mAppSecret = "35ce7a609ab90a5937932da605d47de4";
        mXimalaya.init(this ,mAppSecret);

        if(BaseUtil.isMainProcess(this)) {
            XmDownloadManager.Builder(this)
                    .maxDownloadThread(1)			// 最大的下载个数 默认为1 最大为3
                    .maxSpaceSize(Long.MAX_VALUE)	// 设置下载文件占用磁盘空间最大值，单位字节。不设置没有限制
                    .connectionTimeOut(15000)		// 下载时连接超时的时间 ,单位毫秒 默认 30000
                    .readTimeOut(15000)				// 下载时读取的超时时间 ,单位毫秒 默认 30000
                    .fifo(false)					// 等待队列的是否优先执行先加入的任务. false表示后添加的先执行(不会改变当前正在下载的音频的状态) 默认为true
                    .maxRetryCount(3)				// 出错时重试的次数 默认2次
                    .progressCallBackMaxTimeSpan(1000)//  进度条progress 更新的频率 默认是800
                    .requestTracker(requestTracker)	// 日志 可以打印下载信息
                    .savePath(mp3)	// 保存的地址 会检查这个地址是否有效
                    .create();
        }

        if(BaseUtil.getCurProcessName(this).contains(":player")) {
            XmNotificationCreater instanse = XmNotificationCreater.getInstanse(this);
            instanse.setNextPendingIntent((PendingIntent)null);
            instanse.setPrePendingIntent((PendingIntent)null);
            instanse.setStartOrPausePendingIntent((PendingIntent)null);

            String actionName = "com.app.test.android.Action_Close";
            Intent intent = new Intent(actionName);
            intent.setClass(this, MyPlayerReceiver.class);
            PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, intent, 0);
            instanse.setClosePendingIntent(broadcast);
        }
//        instance = DBUtil.getInstance(this);
    }

    private RequestTracker requestTracker = new RequestTracker() {
        @Override
        public void onWaiting(RequestParams params) {
            Logger.log("TingApplication : onWaiting " + params);
        }

        @Override
        public void onStart(RequestParams params) {
            Logger.log("TingApplication : onStart " + params);
        }

        @Override
        public void onRequestCreated(UriRequest request) {
            Logger.log("TingApplication : onRequestCreated " + request);
        }

        @Override
        public void onSuccess(UriRequest request, Object result) {
            Logger.log("TingApplication : onSuccess " + request + "   result = " + result);
        }

        @Override
        public void onRemoved(UriRequest request) {
            Logger.log("TingApplication : onRemoved " + request);
        }

        @Override
        public void onCancelled(UriRequest request) {
            Logger.log("TingApplication : onCanclelled " + request);
        }

        @Override
        public void onError(UriRequest request, Throwable ex, boolean isCallbackError) {
            Logger.log("TingApplication : onError " + request + "   ex = " + ex + "   isCallbackError = " + isCallbackError);
        }

        @Override
        public void onFinished(UriRequest request) {
            Logger.log("TingApplication : onFinished " + request);
        }
    };

}
