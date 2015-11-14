package com.madeng.wifiqr.espresso;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.jetbrains.annotations.NotNull;

public class ViewUtils {

    public static void rotateScreen(@NotNull final ActivityTestRule activityRule) {
        final Context context = InstrumentationRegistry.getTargetContext();
        final int orientation = context.getResources().getConfiguration().orientation;

        final Activity activity = activityRule.getActivity();
        activity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }
}
