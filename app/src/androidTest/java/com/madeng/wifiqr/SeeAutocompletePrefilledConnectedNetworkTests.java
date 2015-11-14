package com.madeng.wifiqr;

import android.support.test.rule.ActivityTestRule;

import com.madeng.wifiqr.espresso.SpoonScreenshotAction;
import com.madeng.wifiqr.rules.RealmClearRule;
import com.madeng.wifiqr.rules.EmptyRootUtilsRule;
import com.madeng.wifiqr.rules.SingleConenctedSsidWifiUtilsRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class SeeAutocompletePrefilledConnectedNetworkTests {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public EmptyRootUtilsRule rootUtilsRule = new EmptyRootUtilsRule();

    @Rule
    public SingleConenctedSsidWifiUtilsRule wifiUtilsRule = new SingleConenctedSsidWifiUtilsRule("Konnect");

    @Rule
    public RealmClearRule realmClearRule = new RealmClearRule();

    @Test
    public void seeAutocomplete() {
        final String methodName = "seeAutocomplete";
        SpoonScreenshotAction.perform("InitialState", methodName);

        onView(withId(R.id.name_field))
                .check(matches(isDisplayed()))
                .perform(clearText());

        onView(withText(wifiUtilsRule.getSsid()))
                .inRoot(withDecorView(not(is(activityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        SpoonScreenshotAction.perform("AfterSsidClear", methodName);
    }

}