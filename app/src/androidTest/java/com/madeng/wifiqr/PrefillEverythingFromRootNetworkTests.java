package com.madeng.wifiqr;

import android.support.test.rule.ActivityTestRule;

import com.madeng.wifiqr.espresso.SpoonScreenshotAction;
import com.madeng.wifiqr.rules.EvolutionRootUtilsRule;
import com.madeng.wifiqr.rules.RealmClearRule;
import com.madeng.wifiqr.rules.SingleConenctedSsidWifiUtilsRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class PrefillEverythingFromRootNetworkTests {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public EvolutionRootUtilsRule rootUtilsRule = new EvolutionRootUtilsRule();

    @Rule
    public RealmClearRule realmClearRule = new RealmClearRule();

    @Rule
    public SingleConenctedSsidWifiUtilsRule wifiUtilsRule = new SingleConenctedSsidWifiUtilsRule("EvolutionB");

    @Test
    public void testPrefillEverything() {
        final String methodName = "testPrefillEverything";
        SpoonScreenshotAction.perform("InitialState", methodName);

        onView(withId(R.id.main_qr_view))
                .check(matches(isCompletelyDisplayed()));

        SpoonScreenshotAction.perform("FinalState", methodName);
    }
}