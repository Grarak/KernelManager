package com.grarak.kernel.manager.utils;

import android.os.Environment;

import com.grarak.kernel.manager.fragments.BackupFragment;
import com.grarak.kernel.manager.fragments.DownloadFragment;
import com.grarak.kernel.manager.fragments.InformationFragment;
import com.grarak.kernel.manager.fragments.InstallFragment;

/**
 * Created by grarak on 11.10.14.
 */
public interface Constants {

    public final String TAG = "Kernel Manager";
    public final String TEMP_FILE = "/data/local/tmp/kernelmanagertmp";

    public final String KERNEL_VERSION = "/proc/version";
    public final String CPU_INFO = "/proc/cpuinfo";

    public final String sdcard = Environment.getExternalStorageDirectory().getPath();
    public final String kernelmanager_path = sdcard + "/kernelmanager";
    public final String download_path = kernelmanager_path + "/downloads";
    public final String backup_path = kernelmanager_path + "/backups";

    public final KernelUtils mKernelUtils = new KernelUtils();
    public final RootUtils mRootUtils = new RootUtils();
    public final Utils mUtils = new Utils();

    public final BackupFragment mBackupFragment = new BackupFragment();
    public final DownloadFragment mDownloadFragment = new DownloadFragment();
    public final InformationFragment mInformationFragment = new InformationFragment();
    public final InstallFragment mInstallFragment = new InstallFragment();

}
