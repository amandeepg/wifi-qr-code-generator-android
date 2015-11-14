package com.madeng.wifiqr.rules;

import com.madeng.wifiqr.QrNetworkInfo;
import com.madeng.wifiqr.mocks.MockRootUtils;
import com.madeng.wifiqr.utils.RootUtilsFactory;

import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.List;

public class EvolutionRootUtilsRule extends ExternalResource {

    public EvolutionRootUtilsRule() {
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        final List<QrNetworkInfo> networks = new ArrayList<>();
        networks.add(new QrNetworkInfo("EvolutionA", "EvolutionAPwd", QrNetworkInfo.AUTH_WPA, QrNetworkInfo.SOURCE_SAVED));
        networks.add(new QrNetworkInfo("EvolutionB", "EvolutionBPwd", QrNetworkInfo.AUTH_WEP, QrNetworkInfo.SOURCE_SAVED));
        networks.add(new QrNetworkInfo("EvolutionC", "", QrNetworkInfo.AUTH_NONE, QrNetworkInfo.SOURCE_SAVED));

        RootUtilsFactory.setInstance(new MockRootUtils(networks));
    }

    @Override
    protected void after() {
        super.after();
        RootUtilsFactory.setInstance(null);
    }
}
