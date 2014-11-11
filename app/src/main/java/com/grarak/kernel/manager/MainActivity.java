package com.grarak.kernel.manager;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils;


public class MainActivity extends ActionBarActivity implements Constants {

    private CharSequence mTitle;

    private String[] mItems;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "init for " + mUtils.getDeviceCodename());

        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        mItems = getResources().getStringArray(R.array.tabs_arrays);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                getSupportActionBar().getThemedContext(),
                R.layout.simple_list_item_activated_1, R.id.text1,
                getResources().getStringArray(R.array.tabs_arrays)));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) selectItem(0);

        mDrawerToggle.syncState();
    }

    private void selectItem(final int position) {
        mDrawerList.setItemChecked(position, true);

        Fragment fragment = mInformationFragment;

        if (position != 0 && !new JsonUtils.JsonDeviceArrays(mUtils.getDeviceAssetFile(MainActivity.this)).isSupported())
            fragment = mNoSupportFragment;
        else
            switch (position) {
                case 1:
                    fragment = mDownloadFragment;
                    break;
                case 2:
                    fragment = mInstallFragment;
                    break;
                case 3:
                    fragment = mBackupFragment;
                    break;
            }

        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerLayout.closeDrawer(mDrawerList);

        setTitle(mItems[position]);
        mDrawerList.setItemChecked(position, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) super.onBackPressed();
        else mDrawerLayout.openDrawer(mDrawerList);
    }

}
