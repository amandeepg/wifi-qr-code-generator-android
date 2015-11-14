package com.madeng.wifiqr;

public class FillFieldsTask {
    public final String name;

    public FillFieldsTask(String name) {
        this.name = name;
    }

    public FillFieldsTask(QrNetworkInfo info) {
        this(info.getName());
    }
}
