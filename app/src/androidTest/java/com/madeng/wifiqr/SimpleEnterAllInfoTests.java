package com.madeng.wifiqr;

import android.support.annotation.StringRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.madeng.wifiqr.espresso.SpoonScreenshotAction;
import com.madeng.wifiqr.espresso.ViewUtils;
import com.madeng.wifiqr.rules.EmptyWifiUtilsRule;
import com.madeng.wifiqr.rules.RealmClearRule;
import com.madeng.wifiqr.rules.EmptyRootUtilsRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(value = Parameterized.class)
public class SimpleEnterAllInfoTests {

    private final String ssid;
    private final String pwd;
    @StringRes private final int authStrRes;
    private final String authStrDebug;
    private final boolean rotate;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public EmptyRootUtilsRule rootUtilsRule = new EmptyRootUtilsRule();

    @Rule
    public EmptyWifiUtilsRule wifiUtilsRule = new EmptyWifiUtilsRule();

    @Rule
    public RealmClearRule realmClearRule = new RealmClearRule();

    public SimpleEnterAllInfoTests(String ssid, String pwd, int authStrRes, String authStrDebug, boolean rotate) {
        this.ssid = ssid;
        this.pwd = pwd;
        this.authStrRes = authStrRes;
        this.authStrDebug = authStrDebug;
        this.rotate = rotate;
    }

    @Parameterized.Parameters(name = "{0}{1}{3}{4}")
    public static Iterable<Object[]> data1() {
        return Arrays.asList(new Object[][] {
                {"Skynet", "ad23kj4h2kg23hg4", R.string.wep, "WEP", true},
                {"Skynet", "ad23kj4h2kg23hg4", R.string.wpa, "WPA", true},
                {"Skynet", "ad23kj4h2kg23hg4", R.string.none, "None", true},
                {"Skynet", "", R.string.none, "None", true},
                {"Skynet", "ad23kj4h2kg23hg4", R.string.wep, "WEP", false},
                {"Skynet", "ad23kj4h2kg23hg4", R.string.wpa, "WPA", false},
                {"Skynet", "ad23kj4h2kg23hg4", R.string.none, "None", false},
                {"Skynet", "", R.string.none, "None", false},
        });
    }

    @Test
    public void simpleEnterAllInfo() {
        final String testMethod = "simpleEnterAllInfo[" + ssid + pwd + authStrDebug + rotate + "]";
        SpoonScreenshotAction.perform("InitialState", testMethod);

        onView(withId(R.id.name_field))
                .check(matches(isDisplayed()))
                .perform(click());
        SpoonScreenshotAction.perform("AfterSsidClick", testMethod);

        onView(withId(R.id.name_field))
                .perform(clearText())
                .perform(typeText(ssid));
        SpoonScreenshotAction.perform("AfterSsidEntry", testMethod);

        onView(withId(R.id.pwd_field))
                .check(matches(isDisplayed()))
                .perform(clearText())
                .perform(typeText(pwd));
        SpoonScreenshotAction.perform("AfterPwdEntry", testMethod);

        onView(withId(R.id.auth_spinner))
                .check(matches(isDisplayed()))
                .perform(replaceText(InstrumentationRegistry.getTargetContext().getString(authStrRes)));
        SpoonScreenshotAction.perform("AfterAuthEntry", testMethod);

        if (rotate) {
            ViewUtils.rotateScreen(activityRule);
            SpoonScreenshotAction.perform("AfterRotate", testMethod);
        }

        onView(withId(R.id.generate_button))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.main_qr_view))
                .check(matches(isDisplayed()));

        SpoonScreenshotAction.perform("Generated", testMethod);
    }
}