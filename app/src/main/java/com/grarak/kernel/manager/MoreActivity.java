package com.grarak.kernel.manager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonListArrays;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by grarak on 13.10.14.
 */
public class MoreActivity extends Activity implements ActionBar.TabListener {

    private ViewPager mViewPager;

    public static final String ARG_JSON = "json";
    public static final String ARG_JSON_LINK = "jsonlink";
    private String JSON;
    private String JSON_LINK;
    private JsonListArrays mJsonListArrays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.more));

        JSON = getIntent().getExtras().getString(ARG_JSON);
        JSON_LINK = getIntent().getExtras().getString(ARG_JSON_LINK);
        mJsonListArrays = new JsonListArrays(JSON);

        setContentView(R.layout.activity_log);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (actionBar != null) actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
            if (actionBar != null)
                actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
        private PullToRefreshLayout mPullToRefreshLayout;

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

            mWebView = new WebView(getActivity());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new SampleWebViewClient());
            mWebView.clearCache(true);

            mPullToRefreshLayout = new PullToRefreshLayout(getActivity());
            mPullToRefreshLayout.addView(mWebView);
            ActionBarPullToRefresh.from(getActivity())
                    .allChildrenArePullable()
                    .listener(new OnRefreshListener() {
                        @Override
                        public void onRefreshStarted(View view) {
                            mWebView.reload();
                        }
                    })
                    .setup(mPullToRefreshLayout);

            mWebView.loadUrl(link);

            return mPullToRefreshLayout;
        }

        private class SampleWebViewClient extends WebViewClient {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (mPullToRefreshLayout.isRefreshing())
                    mPullToRefreshLayout.setRefreshComplete();
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
