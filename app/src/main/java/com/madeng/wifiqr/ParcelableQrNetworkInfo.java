package com.madeng.wifiqr;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class ParcelableQrNetworkInfo implements Parcelable {

    public final String name;
    public final String password;
    @QrNetworkInfo.AuthType public final int auth;
    @QrNetworkInfo.SourceType public final int source;

    public ParcelableQrNetworkInfo(@NotNull QrNetworkInfo info) {
        this.name = info.getName();
        this.password = info.getPassword();
        this.auth = info.getAuth();
        this.source = info.getSource();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.password);
        dest.writeInt(this.auth);
        dest.writeInt(this.source);
    }

    protected ParcelableQrNetworkInfo(Parcel in) {
        this.name = in.readString();
        this.password = in.readString();
        //noinspection ResourceType
        this.auth = in.readInt();
        //noinspection ResourceType
        this.source = in.readInt();
    }

    public static final Parcelable.Creator<ParcelableQrNetworkInfo> CREATOR = new Parcelable.Creator<ParcelableQrNetworkInfo>() {
        public ParcelableQrNetworkInfo createFromParcel(Parcel source) {
            return new ParcelableQrNetworkInfo(source);
        }

        public ParcelableQrNetworkInfo[] newArray(int size) {
            return new ParcelableQrNetworkInfo[size];
        }
    };
}
