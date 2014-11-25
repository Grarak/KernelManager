package com.grarak.kernel.manager.tasks;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.grarak.kernel.manager.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by grarak on 12.10.14.
 */
public class WebpageReaderTask extends AsyncTask<String, Void, String> implements Constants {

    private final WebpageReaderInterface webpageReaderInterface;

    public WebpageReaderTask(WebpageReaderInterface webpageReaderInterface) {
        this.webpageReaderInterface = webpageReaderInterface;
    }

    @Override
    protected String doInBackground(String... urls) {
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            String line = "";
            URL url = new URL(urls[0]);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null)
                sb.append(line + "\n");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "unable to read: " + urls[0]);
        } finally {
            try {
                if (is != null) is.close();
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString() != null ? sb.toString() : "";
    }

    @Override
    protected void onPostExecute(String result) {
        webpageReaderInterface.webpageResult(result, Html.fromHtml(result).toString());
    }

    public interface WebpageReaderInterface {
        public void webpageResult(String raw, String html);
    }

}
