package com.madeng.wifiqr.rules;

import com.madeng.wifiqr.QrNetworkInfo;
import com.madeng.wifiqr.mocks.MockWifiUtils;
import com.madeng.wifiqr.utils.WifiUtilsFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.List;

public class SingleConenctedSsidWifiUtilsRule extends ExternalResource {

    private final String ssid;

    public SingleConenctedSsidWifiUtilsRule(String ssid) {
        this.ssid = ssid;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        final List<QrNetworkInfo> scannedSsids = new ArrayList<>();
        scannedSsids.add(new QrNetworkInfo(getSsid(), QrNetworkInfo.SOURCE_NEARBY));

        final List<QrNetworkInfo> confSsids = new ArrayList<>();
        confSsids.add(new QrNetworkInfo(getSsid(), QrNetworkInfo.SOURCE_REMEMBERED));

        WifiUtilsFactory.setInstance(new MockWifiUtils(confSsids, scannedSsids, getSsid()));
    }

    @Override
    protected void after() {
        super.after();
        WifiUtilsFactory.setInstance(null);
    }

    @NotNull
    public String getSsid() {
        return ssid;
    }
}
