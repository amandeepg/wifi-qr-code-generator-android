package com.madeng.wifiqr.utils;

public class RootUtilsFactory {
    private static RootUtils sInstance;

    public static RootUtils getInstance() {
        if (sInstance == null) {
            setInstance(new RootUtilsImpl());
        }
        return sInstance;
    }

    public static void setInstance(RootUtils instance) {
        sInstance = instance;
    }
}
