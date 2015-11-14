package com.madeng.wifiqr.rules;

import com.madeng.wifiqr.mocks.MockRootUtils;
import com.madeng.wifiqr.utils.RootUtilsFactory;

import org.junit.rules.ExternalResource;

public class EmptyRootUtilsRule extends ExternalResource {

    public EmptyRootUtilsRule() {
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        RootUtilsFactory.setInstance(new MockRootUtils());
    }

    @Override
    protected void after() {
        super.after();
        RootUtilsFactory.setInstance(null);
    }
}
