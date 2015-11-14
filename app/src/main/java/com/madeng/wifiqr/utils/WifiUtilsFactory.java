package com.madeng.wifiqr.utils;

public class WifiUtilsFactory {

    private static WifiUtils sInstance;

    public static WifiUtils getInstance() {
        if (sInstance == null) {
            setInstance(new WifiUtilsImpl());
        }
        return sInstance;
    }

    public static void setInstance(WifiUtils instance) {
        sInstance = instance;
    }
}
