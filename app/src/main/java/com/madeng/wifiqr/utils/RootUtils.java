package com.madeng.wifiqr.utils;

import android.content.Context;

import com.madeng.wifiqr.QrNetworkInfo;

import org.jetbrains.annotations.NotNull;

import rx.Observable;

public interface RootUtils {

    @NotNull
    Observable<QrNetworkInfo> tryRootNetworks(@NotNull Context context);
}
