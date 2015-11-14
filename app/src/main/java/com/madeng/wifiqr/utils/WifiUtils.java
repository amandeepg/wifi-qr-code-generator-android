package com.madeng.wifiqr.utils;

import android.content.Context;

import com.madeng.wifiqr.QrNetworkInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface WifiUtils {

    @NotNull
    List<QrNetworkInfo> getRememberedNetworks(@NotNull final Context context);

    @NotNull
    List<QrNetworkInfo> getScannedNetworks(@NotNull final Context context);

    @Nullable
    String getConnectedSsid(@NotNull final Context context);
}
