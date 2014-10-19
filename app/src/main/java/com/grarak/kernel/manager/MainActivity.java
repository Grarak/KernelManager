package com.grarak.kernel.manager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import com.grarak.kernel.manager.fragments.NavigationDrawerFragment;
import com.grarak.kernel.manager.fragments.NoSupportFragment;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonDeviceArrays;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, Constants {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        Log.i(TAG, "init for " + mUtils.getDeviceCodename());
    }

    @Override
    public void onNavigationDrawerItemSelected(final int position) {
        // update the main content by replacing fragments
        new Thread() {
            public void run() {
                Fragment fragment = mInformationFragment;
                if (!new JsonDeviceArrays(mUtils.getDeviceAssetFile(MainActivity.this))
                        .isSupported() && position != 0)
                    fragment = new NoSupportFragment();
                else switch (position) {
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

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            super.onBackPressed();
        else
            mNavigationDrawerFragment.openDrawer();
    }

}
