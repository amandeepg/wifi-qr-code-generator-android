package com.madeng.wifiqr.rules;

import com.madeng.wifiqr.mocks.MockWifiUtils;
import com.madeng.wifiqr.utils.WifiUtilsFactory;

import org.junit.rules.ExternalResource;

import java.util.Collections;

public class EmptyWifiUtilsRule extends ExternalResource {

    public EmptyWifiUtilsRule() {
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        WifiUtilsFactory.setInstance(new MockWifiUtils(Collections.emptyList(), Collections.emptyList(), null));
    }

    @Override
    protected void after() {
        super.after();
        WifiUtilsFactory.setInstance(null);
    }
}
