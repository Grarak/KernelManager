package com.grarak.kernel.manager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonListArrays;

/**
 * Created by grarak on 13.10.14.
 */
public class LogActivity extends ActionBarActivity {

    private ViewPager mViewPager;

    public static final String ARG_JSON = "json";
    private String JSON;
    private JsonListArrays mJsonListArrays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        JSON = getIntent().getExtras().getString(ARG_JSON);
        mJsonListArrays = new JsonListArrays(JSON);

        setContentView(R.layout.activity_log);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.more));

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(mJsonListArrays.getLength());

        ((PagerTabStrip) findViewById(R.id.pager_tab_strip)).setTabIndicatorColor(
                getResources().getColor(android.R.color.white));

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WebViewFragment.newInstance(mJsonListArrays.getUrl(position));
        }

        @Override
        public int getCount() {
            return mJsonListArrays.getLength();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mJsonListArrays.getName(position);
        }
    }

    public static class WebViewFragment extends Fragment implements Constants {

        private static final String ARG_WEB_LINK = "link";
        private WebView mWebView;
        private SwipeRefreshLayout refreshLayout;

        public static WebViewFragment newInstance(String link) {
            WebViewFragment fragment = new WebViewFragment();
            Bundle args = new Bundle();
            args.putString(ARG_WEB_LINK, link);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final String link = getArguments().getString(ARG_WEB_LINK);

            LinearLayout layout = new LinearLayout(getActivity());

            refreshLayout = new SwipeRefreshLayout(getActivity());
            refreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_red_dark));
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mWebView.reload();
                }
            });

            layout.addView(refreshLayout);

            mWebView = new WebView(getActivity());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new SampleWebViewClient());
            mWebView.clearCache(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.loadUrl(link);

            refreshLayout.addView(mWebView);

            return layout;
        }

        private class SampleWebViewClient extends WebViewClient {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (refreshLayout.isRefreshing())
                    refreshLayout.setRefreshing(false);
            }
        }
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
