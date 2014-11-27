package com.grarak.kernel.manager.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils;

/**
 * Created by willi on 27.11.14.
 */
public class BootReceiver extends BroadcastReceiver implements Constants {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (new JsonUtils.JsonDeviceArrays(mUtils.getDeviceAssetFile(context)).isSupported())
            context.startActivity(new Intent(context, OTAService.class));
    }
}
