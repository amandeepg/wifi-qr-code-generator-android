package com.madeng.wifiqr.utils.rx;

import rx.functions.Func1;

public class RxUtils {

    public static <T> Func1<T, Boolean> isNotNull() {
        return o -> o != null;
    }
}
