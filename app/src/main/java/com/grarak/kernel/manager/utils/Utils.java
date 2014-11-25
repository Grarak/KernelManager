package com.grarak.kernel.manager.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.grarak.kernel.manager.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by grarak on 11.10.14.
 */
public class Utils implements Constants {

    private String DEVICE_ASSET;

    public String getDeviceAssetFile(Context context) {
        if (DEVICE_ASSET == null) DEVICE_ASSET = readAssetFile(context, "devices.json");
        return DEVICE_ASSET;
    }

    public void confirm(Context context, String title, String message, final OnConfirmListener onConfirmListener) {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);
        if (title != null) dialogBuilder.title(title);
        if (message != null) dialogBuilder.content(message);
        dialogBuilder.negativeText(context.getString(R.string.cancel))
                .positiveText(context.getString(R.string.ok))
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onNegative(MaterialDialog materialDialog) {
                        onConfirmListener.onCancel();
                    }

                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        onConfirmListener.onConfirm();
                    }
                }).show();
    }

    public interface OnConfirmListener {
        public void onConfirm();

        public void onCancel();
    }

    public void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public String readFile(String filepath) {
        try {
            BufferedReader buffreader = new BufferedReader(new FileReader(
                    filepath), 256);
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            buffreader.close();
            return text.toString().trim();
        } catch (FileNotFoundException e) {
            Log.e(TAG, filepath + "does not exist");
        } catch (IOException e) {
            Log.e(TAG, "I/O read error: " + filepath);
        }
        return null;
    }

    public String getDeviceCodename() {
        return Build.DEVICE;
    }

    private String readAssetFile(Context context, String file) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(file);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst) isFirst = false;
                else buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "unable to read " + file);
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                Log.e(TAG, "unable to close Reader " + file);
            }
        }
        return null;
    }
}
