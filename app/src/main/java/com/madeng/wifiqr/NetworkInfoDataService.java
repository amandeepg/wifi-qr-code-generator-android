package com.madeng.wifiqr;

import android.content.Context;

import com.madeng.wifiqr.utils.rx.RealmObservable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Observable;

public class NetworkInfoDataService {

    @NotNull private final Context mContext;

    public NetworkInfoDataService(@NotNull Context context) {
        mContext = context;
    }

    @NotNull
    public Observable<QrNetworkInfo> networkInfo(@NotNull final String name) {
        return RealmObservable
                .object(mContext, realm -> realm.where(QrNetworkInfo.class).equalTo("name", name).findFirst())
                .map(QrNetworkInfo::cloneFromRealm);
    }

    public Observable<List<QrNetworkInfo>> networkInfos() {
        return RealmObservable
                .results(mContext, realm -> realm.where(QrNetworkInfo.class).findAll())
                .map(realmInfos -> {
                    final List<QrNetworkInfo> infos = new ArrayList<>(realmInfos.size());
                    for (QrNetworkInfo realmInfo : realmInfos) {
                        infos.add(QrNetworkInfo.cloneFromRealm(realmInfo));
                    }
                    return infos;
                });
    }

    public void save(@NotNull final QrNetworkInfo info) {
        final Realm realm = Realm.getInstance(mContext);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(info);
        realm.commitTransaction();
    }

}
