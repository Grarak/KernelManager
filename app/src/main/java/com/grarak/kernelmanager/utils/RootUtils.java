package com.grarak.kernelmanager.utils;

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by grarak on 15.10.14.
 */
public class RootUtils implements Constants {

    public void runCommand(final String command) {
        new Thread() {
            public void run() {
                try {
                    RootTools.getShell(true).add(new CommandCapture(0, command)).commandCompleted(0, 0);
                    Log.i(TAG, "open shell: " + command);
                } catch (IOException e) {
                    Log.e(TAG, "failed to run " + command);
                } catch (TimeoutException ignored) {
                    Log.e(TAG, "Timeout: Cannot gain root access");
                } catch (RootDeniedException e) {
                    Log.e(TAG, "Root access denied");
                }
            }
        }.start();
    }

    public void openrecoveryscript(String script) {
        runCommand("mkdir -p /cache/recovery/");
        runCommand("echo " + script + " >> /cache/recovery/openrecoveryscript");
    }

}
