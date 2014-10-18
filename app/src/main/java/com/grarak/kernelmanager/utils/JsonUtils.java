package com.grarak.kernelmanager.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by grarak on 11.10.14.
 */
public class JsonUtils implements Constants {

    public static class JsonDeviceArrays {

        private final String JSON;
        private JSONObject JSON_DEVICE;
        private String[] KERNELS;

        public JsonDeviceArrays(String json) {
            JSON = json;
        }

        public String getBootPartition() {
            try {
                return getDeviceJson().getString("boot");
            } catch (JSONException e) {
                Log.e(TAG, "unable to read boot partition");
            }
            return null;
        }

        public String getKernelDescription(String kernel) {
            return getKernelObjects(kernel, "description");
        }

        public String getKernelJson(String kernel) {
            return getKernelObjects(kernel, "json");
        }

        private String getKernelObjects(String kernel, String object) {
            for (int i = 0; i < getDeviceKernels().length; i++)
                if (kernel.equals(getDeviceKernels()[i])) {
                    try {
                        JSONArray kernelsJSON = getDeviceJson().getJSONArray("kernels");

                        return kernelsJSON.getJSONObject(i).getString(object);
                    } catch (JSONException e) {
                        Log.e(TAG, "unable to read kernels");
                    }
                }
            return null;
        }

        public String[] getDeviceKernels() {
            if (KERNELS == null)
                try {
                    JSONArray kernelsJSON = getDeviceJson().getJSONArray("kernels");

                    KERNELS = new String[kernelsJSON.length()];
                    for (int i = 0; i < kernelsJSON.length(); i++)
                        KERNELS[i] = kernelsJSON.getJSONObject(i).getString("name");
                } catch (JSONException e) {
                    Log.e(TAG, "unable to read kernels");
                }

            return KERNELS;
        }

        public boolean isSupported() {
            return getDeviceJson() != null;
        }

        private JSONObject getDeviceJson() {
            if (JSON_DEVICE == null) {
                if (JSON == null) return null;

                try {
                    JSONObject jsonObj = new JSONObject(JSON);
                    JSONArray devices = jsonObj.getJSONArray("devices");

                    for (int i = 0; i < devices.length(); i++) {
                        JSONObject device = devices.getJSONObject(i);

                        JSONArray codenames = device.getJSONArray("codenames");

                        for (int x = 0; x < codenames.length(); x++)
                            if (codenames.getString(x).equals(mUtils.getDeviceCodename()))
                                JSON_DEVICE = device;
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "unable to read devices JSON");
                }
            }
            return JSON_DEVICE;
        }

    }

    public static class JsonListArrays {

        private final String JSON;

        public JsonListArrays(String json) {
            JSON = json;
        }

        public String getName(int position) {
            return getString(position, "name");
        }

        public String getUrl(int position) {
            return getString(position, "url");
        }

        private String getString(int position, String object) {
            try {
                JSONObject download = getLists().getJSONObject(position);

                return download.getString(object);
            } catch (JSONException e) {
                Log.e(TAG, "unable to read lists");
            }
            return null;
        }

        public int getLength() {
            return getLists().length();
        }

        private JSONArray getLists() {
            try {
                JSONObject downloads = new JSONObject(JSON);

                return downloads.getJSONArray("lists");
            } catch (JSONException e) {
                Log.e(TAG, "unable to read lists");
            }
            return null;
        }

        public boolean isUseable() {
            return getLists() != null;
        }

    }

    public static class JsonDownloadArrays {

        private final String JSON;

        public JsonDownloadArrays(String json) {
            JSON = json;
        }

        public String getUrl(int position) {
            return getString(position, "url");
        }

        public String getNote(int position) {
            return getString(position, "note");
        }

        public String[] getAndroidVersions(int position) {
            try {
                JSONArray versionsJson = getDownloads().getJSONObject(position).getJSONArray("android");
                String[] versions = new String[versionsJson.length()];
                for (int i = 0; i < versions.length; i++)
                    versions[i] = versionsJson.getString(i);

                return versions;
            } catch (JSONException e) {
                Log.e(TAG, "unable to read downloads");
            }
            return null;
        }

        public String getVersion(int position) {
            return getString(position, "version");
        }

        private String getString(int position, String object) {
            try {
                JSONObject download = getDownloads().getJSONObject(position);

                return download.getString(object);
            } catch (JSONException e) {
                Log.e(TAG, "unable to read downloads");
            }
            return null;
        }

        public int getLength() {
            return getDownloads().length();
        }

        private JSONArray getDownloads() {
            try {
                JSONObject downloads = new JSONObject(JSON);

                return downloads.getJSONArray("downloads");
            } catch (JSONException e) {
                Log.e(TAG, "unable to read downloads");
            }
            return null;
        }

        public boolean isUseable() {
            return getDownloads() != null;
        }

    }

}
