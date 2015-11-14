package com.madeng.wifiqr.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.madeng.wifiqr.App;
import com.madeng.wifiqr.QrNetworkInfo;
import com.madeng.wifiqr.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RootUtilsImpl implements RootUtils {

    private static final String ROOT_SCAN_PERMISSION_ASKED = "root_scan_permission_asked";
    private static final String ROOT_SCAN_PERMISSION_GIVEN = "root_scan_permission_given";

    private static final String[] ROOT_LOCATIONS = new String[] {
            "/sbin/",
            "/system/bin/",
            "/system/xbin/",
            "/data/local/xbin/",
            "/data/local/bin/",
            "/system/sd/xbin/",
            "/system/bin/failsafe/",
            "/data/local/"
    };
    private static final String CAT_SCRIPT = "if [ -f /data/misc/wifi/wpa_supplicant.conf ] \n" +
            "then \n" +
            "cat /data/misc/wifi/wpa_supplicant.conf \n" +
            "fi \n" +
            "if [ -f /data/wifi/bcm_supp.conf ] \n" +
            "then \n" +
            "cat /data/wifi/bcm_supp.conf \n" +
            "fi \n" +
            "if [ -f /data/misc/wifi/wpa.conf ] \n" +
            "then \n" +
            "cat /data/misc/wifi/wpa.conf \n" +
            "fi \n";

    private static List<QrNetworkInfo> sCachedWifiNetworks;

    @Override
    @NotNull
    public Observable<QrNetworkInfo> tryRootNetworks(@NotNull final Context context) {
        if (!isRooted()) {
            return Observable.empty();
        }

        return askForRootIfNecessary(context)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(granted -> {
                    if (!granted) {
                        return null;
                    }
                    return readRootWifiFilesObservable();
                })
                .doOnNext(info -> Timber.d("tryRootNetworks %s", info.getName()));
    }

    /**
     * Looks for any signs of device being rooted (ie. that "su" binary exists)
     * in an executable location.
     *
     * @return if rooted or not
     */
    private static boolean isRooted() {
        for (final String where : ROOT_LOCATIONS) {
            final File file = new File(where + "su");
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static List<QrNetworkInfo> readRootWifiFiles() {
        if (sCachedWifiNetworks != null) {
            return sCachedWifiNetworks;
        }

        Timber.d("readRootWifiFiles on %s", Thread.currentThread());
        final List<String> result = Shell.SU.run(CAT_SCRIPT);
        // Size will be greater than 2, when some networks exist
        if (result.size() > 2) {
            sCachedWifiNetworks = parseNetworks(result);
        } else {
            sCachedWifiNetworks = Collections.emptyList();
        }
        return sCachedWifiNetworks;
    }

    @NotNull
    private static List<QrNetworkInfo> parseNetworks(@NotNull final List<String> list) {
        final List<QrNetworkInfo> infos = new ArrayList<>();
        // Split along "network="
        StringBuilder s = new StringBuilder();
        for (final String ss : list) {
            s.append(ss).append("\n");
        }
        String sa[] = s.toString().split("network=");

        for (int x = 1; x < sa.length; x++) {
            try {
                String type = "WPA-PSK";
                try {
                    type = networkType(sa[x]);
                } catch (Exception ignored) {
                }
                String pass = "";
                boolean use = true;
                int auth = 2;
                if (type == null) {
                    continue;
                }
                switch (type) {
                    case "WPA-PSK":
                        try {
                            pass = findLine(sa[x], "psk=");
                            auth = 0;
                        } catch (Exception ignored) {
                        }
                        break;
                    case "WEP":
                        try {
                            pass = findLine(sa[x], "wep_key0=");
                            auth = 1;
                        } catch (Exception ignored) {
                        }
                        break;
                    case "NONE":
                        break;
                    default:
                        use = false;
                        break;
                }

                if (use) {
                    String ssid = findLine(sa[x], "ssid=");
                    ssid = WifiUtilsImpl.cleanQuotes(ssid);
                    pass = WifiUtilsImpl.cleanQuotes(pass);
                    if (ssid.isEmpty() || pass.isEmpty()) {
                        continue;
                    }
                    infos.add(new QrNetworkInfo(ssid, pass, auth, QrNetworkInfo.SOURCE_SAVED));
                }
            } catch (Exception ignored) {
                Timber.d("ignored root network");
            }
        }
        return infos;
    }

    /**
     * Determines type of network
     *
     * @param s string containing all info about network (wpa_supplicant.conf
     *          style)
     * @return type of network
     */
    @Nullable
    private static String networkType(@NotNull final String s) {
        String type = findLine(s, "key_mgmt=");

        if ("NONE".equals(type)) {
            String type2 = findLine(s, "auth_alg=");
            if (type2 != null && type2.equals("OPEN SHARED")) {
                return "WEP";
            }
        }

        return type;
    }

    /**
     * Finds part of string from needle to end of line
     *
     * @param haystack what we're searching in
     * @param needle   what we're searching for
     * @return part of string from needle to end of line
     */
    @Nullable
    private static String findLine(@NotNull final String haystack, @NotNull final String needle) {
        int i = haystack.indexOf(needle) + needle.length();
        if (i == (-1 + needle.length())) {
            return null;
        }
        int i2 = haystack.indexOf('\n', i);
        return haystack.substring(i, i2);
    }

    @NotNull
    private static Observable<Boolean> askForRootIfNecessary(@NotNull final Context context) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                final SharedPreferences prefs = context.getSharedPreferences("RootUtils", Context.MODE_PRIVATE);

                if (prefs.getBoolean(ROOT_SCAN_PERMISSION_ASKED, false)) {
                    subscriber.onNext(prefs.getBoolean(ROOT_SCAN_PERMISSION_GIVEN, false));
                    subscriber.onCompleted();
                    return;
                }

                new AlertDialog.Builder(context)
                        .setMessage(R.string.root_detected_message)
                        .setPositiveButton(R.string.yes, (dialog, id) -> {
                            prefs.edit()
                                    .putBoolean(ROOT_SCAN_PERMISSION_ASKED, true)
                                    .putBoolean(ROOT_SCAN_PERMISSION_GIVEN, true)
                                    .apply();
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                            sendAnalyticsRootPermission(context, true, true);
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                            prefs.edit()
                                    .putBoolean(ROOT_SCAN_PERMISSION_ASKED, true)
                                    .putBoolean(ROOT_SCAN_PERMISSION_GIVEN, false)
                                    .apply();
                            dialog.cancel();
                            subscriber.onNext(false);
                            subscriber.onCompleted();
                            sendAnalyticsRootPermission(context, true, false);
                        }).show();
            }
        });
    }

    private static void sendAnalyticsRootPermission(@NotNull final Context context, boolean asked, boolean granted) {
        Timber.d("ROOT PERMISSION: asked = %s, given: %s", asked, granted);
        final String askedStr = asked ? "Asked" : "Given";
        final String grantedStr = granted ? "Granted" : "Denied";
        App.getAnswers(context).logCustom(new CustomEvent("Access Root")
                .putCustomAttribute("Asked", askedStr)
                .putCustomAttribute("Given", grantedStr));
        sendGaEvent(context, "Permission", "Access Root", askedStr + grantedStr);
    }

    private static void sendGaEvent(@NotNull final Context context,
                                    @NotNull final String category,
                                    @NotNull final String action,
                                    @NotNull final String label) {
        App.getTracker(context).send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    @NotNull
    private static Observable<QrNetworkInfo> readRootWifiFilesObservable() {
        return Observable.defer(() -> Observable.from(readRootWifiFiles()));
    }
}
