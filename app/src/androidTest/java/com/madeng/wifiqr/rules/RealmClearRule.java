package com.madeng.wifiqr.rules;

import android.support.test.InstrumentationRegistry;

import com.madeng.wifiqr.QrNetworkInfo;

import org.junit.rules.ExternalResource;

import io.realm.Realm;

public class RealmClearRule extends ExternalResource {

    public RealmClearRule() {
    }

    @Override
    protected void before() throws Throwable {
        final Realm realm = Realm.getInstance(InstrumentationRegistry.getTargetContext());
        realm.beginTransaction();
        realm.allObjects(QrNetworkInfo.class).clear();
        realm.commitTransaction();
    }
}
