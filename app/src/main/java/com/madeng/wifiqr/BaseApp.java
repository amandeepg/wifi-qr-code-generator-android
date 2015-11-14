package com.madeng.wifiqr;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.jetbrains.annotations.NotNull;

import io.fabric.sdk.android.Fabric;

public class BaseApp extends Application {
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        initFabric(this);
        getTracker(this);
        getAnswers(this);
    }

    private static void initFabric(@NotNull final Context context) {
        if (!Fabric.isInitialized()) {
            Fabric.with(context, new Crashlytics());
        }
    }

    public static Answers getAnswers(@NotNull final Context context) {
        initFabric(context);
        return Answers.getInstance();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized static public Tracker getTracker(@NotNull final Context context) {
        if (sTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            sTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return sTracker;
    }
}
