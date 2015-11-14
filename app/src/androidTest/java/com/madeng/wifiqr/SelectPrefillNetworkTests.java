package com.madeng.wifiqr;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;

import com.madeng.wifiqr.espresso.SpoonScreenshotAction;
import com.madeng.wifiqr.rules.EmptyRootUtilsRule;
import com.madeng.wifiqr.rules.EvolutionWifiUtilsRule;
import com.madeng.wifiqr.rules.RealmClearRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class SelectPrefillNetworkTests {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public EmptyRootUtilsRule rootUtilsRule = new EmptyRootUtilsRule();

    @Rule
    public RealmClearRule realmClearRule = new RealmClearRule();

    @Rule
    public EvolutionWifiUtilsRule wifiUtilsRule = new EvolutionWifiUtilsRule();

    @Test
    public void testPopupTap() {
        final String methodName = "testPopupTap";
        SpoonScreenshotAction.perform("InitialState", methodName);

        onView(withId(R.id.name_field))
                .check(matches(isDisplayed()))
                .perform(click());

        onPopupItemView("EvolutionA")
                .check(matches(isDisplayed()));
        onPopupItemView("EvolutionB")
                .check(matches(isDisplayed()));
        onPopupItemView("EvolutionC")
                .check(matches(isDisplayed()));

        SpoonScreenshotAction.perform("AfterSsidClear", methodName);

        onPopupItemView("EvolutionC")
                .perform(click());

        SpoonScreenshotAction.perform("AfterPopupSelect", methodName);

        onView(withId(R.id.name_field))
                .check(matches(withText("EvolutionC")));
    }

    public ViewInteraction onPopupItemView(String itemText) {
        return onView(withText(itemText))
                .inRoot(withDecorView(not(is(activityRule.getActivity().getWindow().getDecorView()))));
    }

}