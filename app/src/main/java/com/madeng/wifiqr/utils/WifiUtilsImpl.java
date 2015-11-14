package com.madeng.wifiqr.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.madeng.wifiqr.QrNetworkInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class WifiUtilsImpl implements WifiUtils {

    @NotNull
    @Override
    public List<QrNetworkInfo> getRememberedNetworks(@NotNull final Context context) {
        Timber.d("getRememberedNetworks on %s", Thread.currentThread());
        final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return Collections.emptyList();
        }
        final List<WifiConfiguration> configuredNetworks = wifi.getConfiguredNetworks();
        if (configuredNetworks == null) {
            return Collections.emptyList();
        }

        final List<QrNetworkInfo> confSsids = new ArrayList<>();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf == null) {
                continue;
            }
            final String name = cleanQuotes(conf.SSID);
            if (name.isEmpty()) {
                continue;
            }
            confSsids.add(new QrNetworkInfo(name, QrNetworkInfo.SOURCE_REMEMBERED));
            Timber.d("getRememberedNetworks %s", name);

        }
        return confSsids;
    }

    @NotNull
    @Override
    public List<QrNetworkInfo> getScannedNetworks(@NotNull final Context context) {
        Timber.d("getScannedNetworks on %s", Thread.currentThread());
        final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return Collections.emptyList();
        }
        final List<ScanResult> scanResults = wifi.getScanResults();
        if (scanResults == null) {
            return Collections.emptyList();
        }

        final ArrayList<QrNetworkInfo> scannedSsids = new ArrayList<>();
        for (final ScanResult scanr : scanResults) {
            if (scanr == null) {
                continue;
            }
            final String name = cleanQuotes(scanr.SSID);
            if (name.isEmpty()) {
                continue;
            }
            scannedSsids.add(new QrNetworkInfo(name, QrNetworkInfo.SOURCE_NEARBY));
            Timber.d("getScannedNetworks %s", name);
        }
        return scannedSsids;
    }

    @Nullable
    @Override
    public String getConnectedSsid(@NotNull final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        if (isConnected && isWiFi && connectionInfo != null) {
            return cleanQuotes(connectionInfo.getSSID());
        } else {
            return null;
        }
    }

    @NotNull
    public static String cleanQuotes(@Nullable final String ssid) {
        if (ssid == null) {
            return "";
        }
        return ssid.replace("\"", "").trim();
    }
}
