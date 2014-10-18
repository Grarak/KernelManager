package com.grarak.kernelmanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import com.grarak.kernelmanager.elements.CustomCard.DescriptionCard;
import com.grarak.kernelmanager.elements.CustomCardArrayAdapter;
import com.grarak.kernelmanager.tasks.DownloadTask;
import com.grarak.kernelmanager.utils.Constants;
import com.grarak.kernelmanager.utils.JsonUtils.JsonDownloadArrays;
import com.grarak.kernelmanager.utils.Utils;
import com.grarak.kernelmanager.utils.WebpageReaderTask;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by grarak on 12.10.14.
 */
public class DownloadActivity extends Activity implements Constants {

    public static final String ARG_KERNEL_NAME = "kernelname";
    public static final String ARG_JSON = "json";
    public static final String ARG_JSON_LINK = "jsonlink";
    private String KERNEL_NAME;
    private String JSON;
    private String JSON_LINK;
    private JsonDownloadArrays mJsonDownloadArrays;

    private CardListView listView;

    private ArrayList<Card> cards = new ArrayList<Card>();
    private CustomCardArrayAdapter mCardArrayAdapter;
    private boolean refresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        KERNEL_NAME = getIntent().getExtras().getString(ARG_KERNEL_NAME);
        JSON = getIntent().getExtras().getString(ARG_JSON);
        JSON_LINK = getIntent().getExtras().getString(ARG_JSON_LINK);
        mJsonDownloadArrays = new JsonDownloadArrays(JSON);

        getActionBar().setTitle(KERNEL_NAME);

        listView = new CardListView(this);

        final PullToRefreshLayout layout = new PullToRefreshLayout(this);
        layout.addView(listView);

        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        refresh = true;
                        runOnUiThread(run);
                        layout.setRefreshComplete();
                    }
                })
                .setup(layout);

        setContentView(layout);

        new Handler().postDelayed(run, 200);
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {

            cards.clear();

            if (refresh)
                new WebpageReaderTask(new WebpageReaderTask.WebpageReaderInterface() {
                    @Override
                    public void webpageResult(String raw, String html) {
                        mJsonDownloadArrays = new JsonDownloadArrays(raw);
                    }
                }).execute(JSON_LINK);

            if (!mJsonDownloadArrays.isUseable()) {
                mUtils.toast(DownloadActivity.this, getString(R.string.no_connection));
                return;
            }

            for (int i = 0; i < mJsonDownloadArrays.getLength(); i++) {
                String description;
                String androidVersions = "";
                String note = mJsonDownloadArrays.getNote(i);
                for (String version : mJsonDownloadArrays.getAndroidVersions(i))
                    androidVersions = androidVersions.isEmpty() ? androidVersions + version : androidVersions
                            + ", " + version;

                description = getString(R.string.android_version) + ": " + androidVersions;
                if (note != null) description = description + "\n" + note;

                final DescriptionCard card = new DescriptionCard(DownloadActivity.this);
                card.setTitle(getString(R.string.version, mJsonDownloadArrays.getVersion(i)));
                card.setDescription(description);
                card.setId(i);
                card.setOnClickListener(new DescriptionCard.CustomOnCardClickListener() {
                    @Override
                    public void onClick(int id) {
                        downloadConfirmation(id);
                    }
                });
                cards.add(card);
            }

            if (mCardArrayAdapter == null)
                mCardArrayAdapter = new CustomCardArrayAdapter(DownloadActivity.this, cards);

            if (refresh) {
                mCardArrayAdapter.notifyDataSetChanged(true);
                refresh = false;
            } else if (listView != null) listView.setAdapter(mCardArrayAdapter);
        }
    };

    private void downloadConfirmation(final int id) {
        mUtils.confirm(this, null, getString(R.string.download_confirm, KERNEL_NAME + " " +
                mJsonDownloadArrays.getVersion(id)), new Utils.OnConfirmListener() {
            @Override
            public void onConfirm() {
                new DownloadTask(DownloadActivity.this, new DownloadTask.DownloadListener() {
                    @Override
                    public void downloadFinish(DownloadTask.DownloadStatus status) {
                        switch (status) {
                            case SUCCESS:
                                mUtils.toast(DownloadActivity.this, getString(R.string.download_completed));
                                break;
                            case CANCELED:
                                mUtils.toast(DownloadActivity.this, getString(R.string.download_canceled));
                                break;
                            case FAILED:
                                mUtils.toast(DownloadActivity.this, getString(R.string.download_failed));
                                break;
                        }
                    }
                }, download_path, KERNEL_NAME + " " + mJsonDownloadArrays.getVersion(id) + ".zip").execute(
                        mJsonDownloadArrays.getUrl(id));
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return true;
    }

}
