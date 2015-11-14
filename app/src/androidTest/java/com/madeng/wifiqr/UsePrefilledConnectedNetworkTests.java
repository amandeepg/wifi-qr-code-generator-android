package com.madeng.wifiqr;

import android.support.annotation.StringRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.madeng.wifiqr.espresso.SpoonScreenshotAction;
import com.madeng.wifiqr.espresso.ViewUtils;
import com.madeng.wifiqr.rules.RealmClearRule;
import com.madeng.wifiqr.rules.EmptyRootUtilsRule;
import com.madeng.wifiqr.rules.SingleConenctedSsidWifiUtilsRule;

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
public class UsePrefilledConnectedNetworkTests {

    private final String pwd;
    @StringRes private final int authStrRes;
    private final String authStrDebug;
    private final boolean rotate;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public EmptyRootUtilsRule rootUtilsRule = new EmptyRootUtilsRule();

    @Rule
    public SingleConenctedSsidWifiUtilsRule wifiUtilsRule = new SingleConenctedSsidWifiUtilsRule("Konnect");

    @Rule
    public RealmClearRule realmClearRule = new RealmClearRule();

    public UsePrefilledConnectedNetworkTests(String pwd, int authStrRes, String authStrDebug, boolean rotate) {
        this.pwd = pwd;
        this.authStrRes = authStrRes;
        this.authStrDebug = authStrDebug;
        this.rotate = rotate;
    }

    @Parameterized.Parameters(name = "{0}{2}{3}")
    public static Iterable<Object[]> data1() {
        return Arrays.asList(new Object[][] {
                {"ad23kj4h2kg23hg4", R.string.wep, "WEP", true},
                {"ad23kj4h2kg23hg4", R.string.wpa, "WPA", true},
                {"ad23kj4h2kg23hg4", R.string.none, "None", true},
                {"", R.string.none, "None", true},
                {"ad23kj4h2kg23hg4", R.string.wep, "WEP", false},
                {"ad23kj4h2kg23hg4", R.string.wpa, "WPA", false},
                {"ad23kj4h2kg23hg4", R.string.none, "None", false},
                {"", R.string.none, "None", false},
        });
    }

    @Test
    public void usePrefillSsid() {
        final String testMethod = "usePrefillSsid[" + pwd + authStrDebug + rotate + "]";
        SpoonScreenshotAction.perform("InitialState", testMethod);

        onView(withId(R.id.name_field))
                .check(matches(isDisplayed()));

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