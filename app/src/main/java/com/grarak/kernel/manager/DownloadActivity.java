package com.grarak.kernel.manager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.grarak.kernel.manager.elements.CustomCard.DescriptionCard;
import com.grarak.kernel.manager.elements.CustomCardArrayAdapter;
import com.grarak.kernel.manager.tasks.DownloadTask;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonDownloadArrays;
import com.grarak.kernel.manager.utils.Utils;
import com.grarak.kernel.manager.utils.WebpageReaderTask;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by grarak on 12.10.14.
 */
public class DownloadActivity extends ActionBarActivity implements Constants {

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        KERNEL_NAME = getIntent().getExtras().getString(ARG_KERNEL_NAME);
        JSON = getIntent().getExtras().getString(ARG_JSON);
        JSON_LINK = getIntent().getExtras().getString(ARG_JSON_LINK);
        mJsonDownloadArrays = new JsonDownloadArrays(JSON);

        getSupportActionBar().setTitle(KERNEL_NAME);

        LinearLayout layout = new LinearLayout(this);

        final SwipeRefreshLayout refreshLayout = new SwipeRefreshLayout(this);
        refreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_red_dark));
        layout.addView(refreshLayout);

        listView = new CardListView(this);

        refreshLayout.addView(listView);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                runOnUiThread(run);
                refreshLayout.setRefreshing(false);
            }
        });

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
