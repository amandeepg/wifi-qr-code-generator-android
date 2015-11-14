package com.madeng.wifiqr.mocks;

import android.content.Context;

import com.madeng.wifiqr.QrNetworkInfo;
import com.madeng.wifiqr.utils.WifiUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MockWifiUtils implements WifiUtils {

    @NotNull private final List<QrNetworkInfo> rememberedNetworks;
    @NotNull private final List<QrNetworkInfo> scannedNetworks;
    @Nullable private final String connectedSsid;

    public MockWifiUtils(@NotNull List<QrNetworkInfo> rememberedNetworks,
                         @NotNull List<QrNetworkInfo> scannedNetworks,
                         @Nullable String connectedSsid) {
        this.rememberedNetworks = rememberedNetworks;
        this.scannedNetworks = scannedNetworks;
        this.connectedSsid = connectedSsid;
    }

    @NotNull
    @Override
    public List<QrNetworkInfo> getRememberedNetworks(@NotNull Context context) {
        return rememberedNetworks;
    }

    @NotNull
    @Override
    public List<QrNetworkInfo> getScannedNetworks(@NotNull Context context) {
        return scannedNetworks;
    }

    @Nullable
    @Override
    public String getConnectedSsid(@NotNull Context context) {
        return connectedSsid;
    }
}
