package com.madeng.wifiqr;

import android.support.annotation.StringRes;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;

import com.madeng.wifiqr.espresso.SpoonScreenshotAction;
import com.madeng.wifiqr.espresso.ViewUtils;
import com.madeng.wifiqr.rules.EmptyWifiUtilsRule;
import com.madeng.wifiqr.rules.EvolutionRootUtilsRule;
import com.madeng.wifiqr.rules.RealmClearRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(value = Parameterized.class)
public class SelectPrefillRootNetworkTests {

    private final String ssid;
    private final String pwd;
    @StringRes private final int authStrRes;

    public SelectPrefillRootNetworkTests(String ssid, String pwd, int authStrRes) {
        this.ssid = ssid;
        this.pwd = pwd;
        this.authStrRes = authStrRes;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data1() {
        return Arrays.asList(new Object[][] {
                {"EvolutionA", "EvolutionAPwd", R.string.wpa,},
                {"EvolutionB", "EvolutionBPwd", R.string.wep,},
                {"EvolutionC", "", R.string.none,},
        });
    }

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public EvolutionRootUtilsRule rootUtilsRule = new EvolutionRootUtilsRule();

    @Rule
    public RealmClearRule realmClearRule = new RealmClearRule();

    @Rule
    public EmptyWifiUtilsRule wifiUtilsRule = new EmptyWifiUtilsRule();

    @Test
    public void testPopupTap() {
        final String methodName = "testPopupTap[" + ssid + "]";
        SpoonScreenshotAction.perform("InitialState", methodName);

        onView(withId(R.id.name_field))
                .check(matches(isDisplayed()))
                .perform(clearText());

        ViewUtils.sleep(1000);
        onPopupItemView("EvolutionA")
                .check(matches(isDisplayed()));
        onPopupItemView("EvolutionB")
                .check(matches(isDisplayed()));
        onPopupItemView("EvolutionC")
                .check(matches(isDisplayed()));

        SpoonScreenshotAction.perform("AfterSsidClear", methodName);

        onView(withId(R.id.main_qr_view))
                .check(matches(not(isDisplayed())));
        onPopupItemView(ssid)
                .perform(click());

        SpoonScreenshotAction.perform("AfterPopupSelect", methodName);

        onView(withId(R.id.name_field))
                .check(matches(withText(ssid)));
        onView(withId(R.id.pwd_field))
                .check(matches(withText(pwd)));
        onView(withId(R.id.auth_spinner))
                .check(matches(withText(authStrRes)));

        ViewUtils.sleep(1000);
        onView(withId(R.id.main_qr_view))
                .check(matches(isCompletelyDisplayed()));
    }

    public ViewInteraction onPopupItemView(String itemText) {
        return onView(withText(itemText))
                .inRoot(withDecorView(not(is(activityRule.getActivity().getWindow().getDecorView()))));
    }

}