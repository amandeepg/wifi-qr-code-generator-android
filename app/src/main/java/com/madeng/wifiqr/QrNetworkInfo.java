package com.madeng.wifiqr;

import android.content.res.Resources;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import rx.functions.Func1;

public class QrNetworkInfo extends RealmObject {

    @IntDef({AUTH_WPA, AUTH_WEP, AUTH_NONE, AUTH_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AuthType {}

    @IntDef({SOURCE_NEARBY, SOURCE_SAVED, SOURCE_REMEMBERED, SOURCE_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SourceType {}

    public static final int AUTH_WPA = 0;
    public static final int AUTH_WEP = 1;
    public static final int AUTH_NONE = 2;
    public static final int AUTH_UNKNOWN = 3;

    public static final int SOURCE_NEARBY = 0;
    public static final int SOURCE_SAVED = 1;
    public static final int SOURCE_REMEMBERED = 2;
    public static final int SOURCE_UNKNOWN = 3;

    @PrimaryKey private String name;
    private String password;
    @AuthType private int auth;
    @SourceType private int source;

    public QrNetworkInfo(String name, String password, @AuthType int auth, @SourceType int source) {
        this.name = name;
        this.password = password;
        this.auth = auth;
        this.source = source;
    }

    public QrNetworkInfo(String name, String password, @AuthType int auth) {
        this(name, password, auth, SOURCE_UNKNOWN);
    }

    public QrNetworkInfo() {
        this(null, null, AUTH_UNKNOWN, SOURCE_UNKNOWN);
    }

    public QrNetworkInfo(String name, @SourceType int source) {
        this(name, null, AUTH_UNKNOWN, source);
    }

    public QrNetworkInfo(@NotNull ParcelableQrNetworkInfo info) {
        this.name = info.name;
        this.password = info.password;
        this.auth = info.auth;
        this.source = info.source;
    }

    @NotNull
    public static <T> List<QrNetworkInfo> removeDuplicates(@NotNull List<QrNetworkInfo> list,
                                                           @NotNull Func1<QrNetworkInfo, T> dupeFunc) {
        final Map<T, QrNetworkInfo> map = new LinkedHashMap<>();
        for (final QrNetworkInfo info : list) {
            map.put(dupeFunc.call(info), info);
        }
        return new ArrayList<>(map.values());
    }

    @StringRes
    public static int getAuthStringResRepresentation(@NotNull final QrNetworkInfo info) {
        if (info.getAuth() == AUTH_WPA) {
            return R.string.wpa;
        } else if (info.getAuth() == AUTH_WEP) {
            return R.string.wep;
        } else if (info.getAuth() == AUTH_NONE) {
            return R.string.none;
        }
        throw new IllegalArgumentException("Non-valid auth type given");
    }

    @AuthType
    public static int getAuthFromStringResRepresentation(@NotNull final String authString,
                                                         @NotNull final Resources resources) {
        if (authString.equals(resources.getString(R.string.wpa))) {
            return AUTH_WPA;
        } else if (authString.equals(resources.getString(R.string.wep))) {
            return AUTH_WEP;
        } else if (authString.equals(resources.getString(R.string.none))) {
            return AUTH_NONE;
        } else {
            throw new IllegalArgumentException("Non-valid auth type given");
        }
    }

    @NotNull
    public static QrNetworkInfo cloneFromRealm(@NotNull QrNetworkInfo realmIssue) {
        return new QrNetworkInfo(
                realmIssue.getName(),
                realmIssue.getPassword(),
                realmIssue.getAuth(),
                realmIssue.getSource()
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @AuthType
    public int getAuth() {
        return auth;
    }

    public void setAuth(@AuthType int auth) {
        this.auth = auth;
    }

    @SourceType
    public int getSource() {
        return source;
    }

    public void setSource(@SourceType int source) {
        this.source = source;
    }
}
