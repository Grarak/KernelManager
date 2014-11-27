package com.grarak.kernel.manager.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.grarak.kernel.manager.DownloadActivity;
import com.grarak.kernel.manager.R;
import com.grarak.kernel.manager.tasks.WebpageReaderTask;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils;

/**
 * Created by willi on 27.11.14.
 */
public class OTAService extends Service implements Constants {

    private final Handler hand = new Handler();

    private int ID = 0;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "starting OTA service");

        hand.post(run);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        hand.removeCallbacks(run);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {

            final JsonUtils.JsonDeviceArrays mJsonDeviceArrays = new JsonUtils.JsonDeviceArrays(
                    mUtils.getDeviceAssetFile(OTAService.this));

            for (final String kernel : mJsonDeviceArrays.getDeviceKernels())
                if (mUtils.getBoolean(kernel, false, OTAService.this)) {
                    final String kernelName = kernel;
                    new WebpageReaderTask(new WebpageReaderTask.WebpageListener() {
                        @Override
                        public void webpageResult(String raw, String html) {
                            if (raw.isEmpty() || raw == null) return;

                            JsonUtils.JsonDownloadArrays mJsonDownloadArrays = new JsonUtils.JsonDownloadArrays(raw);
                            int downloadLength = mJsonDownloadArrays.getLength();
                            int currentDownloadLength = mUtils.getInteger(kernelName + "Downloads", -1, OTAService.this);
                            if (currentDownloadLength >= 0 && downloadLength > currentDownloadLength)
                                buildNotification(raw, mJsonDeviceArrays.getKernelJson(kernelName), kernelName, ID++);
                            mUtils.saveInteger(kernelName + "Downloads", downloadLength, OTAService.this);

                        }
                    }).execute(mJsonDeviceArrays.getKernelJson(kernel));
                }

            ID = 0;

            // Check for every 6 hours
            hand.postDelayed(run, OTA_TIME * 3600000);
        }
    };

    private void buildNotification(String json, String link, String kernel, int id) {
        Intent resultIntent = new Intent(this, DownloadActivity.class);

        Bundle args = new Bundle();
        args.putString(DownloadActivity.ARG_KERNEL_NAME, kernel);
        args.putString(DownloadActivity.ARG_JSON, json);
        args.putString(DownloadActivity.ARG_JSON_LINK, link);
        resultIntent.putExtras(args);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.new_download, kernel))
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mBuilder.setPriority(Notification.PRIORITY_MAX);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }

}
