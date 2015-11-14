package com.madeng.wifiqr.mocks;

import android.content.Context;

import com.madeng.wifiqr.QrNetworkInfo;
import com.madeng.wifiqr.utils.RootUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public class MockRootUtils implements RootUtils {

    @NotNull private final List<QrNetworkInfo> networks;

    public MockRootUtils() {
        this(Collections.<QrNetworkInfo>emptyList());
    }

    public MockRootUtils(@NotNull List<QrNetworkInfo> networks) {
        this.networks = networks;
    }

    @NotNull
    @Override
    public Observable<QrNetworkInfo> tryRootNetworks(@NotNull Context context) {
        return Observable.from(networks);
    }
}
