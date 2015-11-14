package com.madeng.wifiqr.rules;

import com.madeng.wifiqr.QrNetworkInfo;
import com.madeng.wifiqr.mocks.MockWifiUtils;
import com.madeng.wifiqr.utils.WifiUtilsFactory;

import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.List;

public class EvolutionWifiUtilsRule extends ExternalResource {

    public EvolutionWifiUtilsRule() {
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        final List<QrNetworkInfo> scannedSsids = new ArrayList<>();
        scannedSsids.add(new QrNetworkInfo("EvolutionA", QrNetworkInfo.SOURCE_NEARBY));
        scannedSsids.add(new QrNetworkInfo("EvolutionC", QrNetworkInfo.SOURCE_NEARBY));

        final List<QrNetworkInfo> confSsids = new ArrayList<>();
        confSsids.add(new QrNetworkInfo("EvolutionA", QrNetworkInfo.SOURCE_REMEMBERED));
        confSsids.add(new QrNetworkInfo("EvolutionB", QrNetworkInfo.SOURCE_REMEMBERED));

        WifiUtilsFactory.setInstance(new MockWifiUtils(confSsids, scannedSsids, null));
    }

    @Override
    protected void after() {
        super.after();
        WifiUtilsFactory.setInstance(null);
    }
}
