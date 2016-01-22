package com.madeng.wifiqr;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.internal.util.UtilityFunctions;

public class NetworkInfoDataService {

    @NotNull private final Context mContext;

    public NetworkInfoDataService(@NotNull Context context) {
        mContext = context;
    }

    @NotNull
    private RealmQuery<QrNetworkInfo> whereNameEqualTo(@NotNull String name) {
        return Realm.getInstance(mContext)
                .where(QrNetworkInfo.class)
                .equalTo("name", name);
    }

    @NotNull
    public QrNetworkInfo networkInfoBlocking(@NotNull final String name) {
        return whereNameEqualTo(name).findFirst();
    }

    @NotNull
    public Observable<QrNetworkInfo> networkInfo(@NotNull final String name) {
        return whereNameEqualTo(name)
                .findFirstAsync()
                .<QrNetworkInfo>asObservable()
                .filter(RealmObject::isLoaded)
                .first()
                .map(QrNetworkInfo::cloneFromRealm);
    }

    public Observable<List<QrNetworkInfo>> networkInfos() {
        return Realm.getInstance(mContext)
                .where(QrNetworkInfo.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .first()
                .flatMapIterable(UtilityFunctions.identity())
                .map(QrNetworkInfo::cloneFromRealm)
                .toList();
    }

    public void save(@NotNull final QrNetworkInfo info) {
        final Realm realm = Realm.getInstance(mContext);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(info);
        realm.commitTransaction();
    }

}
