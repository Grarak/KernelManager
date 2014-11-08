package com.grarak.kernel.manager.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.grarak.kernel.manager.R;
import com.grarak.kernel.manager.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by grarak on 14.10.14.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> implements Constants {

    public enum DownloadStatus {
        SUCCESS, CANCELED, FAILED
    }

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private final DownloadListener listener;

    private final String downloadPath;
    private final String downloadFileName;

    public DownloadTask(Context context, DownloadListener listener, String downloadPath, String downloadFileName) {
        this.context = context;
        this.listener = listener;
        this.downloadPath = downloadPath;
        this.downloadFileName = downloadFileName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        new File(downloadPath).mkdirs();

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(context.getString(R.string.downloading, downloadFileName));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
                new File(downloadPath + "/" + downloadFileName).delete();
                listener.downloadFinish(DownloadStatus.CANCELED);
            }
        });

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getClass().getName());
        mWakeLock.acquire();
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        mProgressDialog.dismiss();

        if (result != null) new File(downloadPath + "/" + downloadFileName).delete();
        listener.downloadFinish(result == null ? DownloadStatus.SUCCESS : DownloadStatus.FAILED);
    }

    @Override
    protected String doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            Log.i(TAG, "downloading " + urls[0] + " to " + downloadPath + "/" + downloadFileName);
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(downloadPath + "/" + downloadFileName);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0) publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (connection != null) connection.disconnect();
        }
        return null;
    }

    public interface DownloadListener {
        public void downloadFinish(DownloadStatus status);
    }

}
