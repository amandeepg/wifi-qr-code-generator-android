package com.madeng.wifiqr.utils.rx;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

public class RxBus {

    private static final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    public static void send(@NotNull Object o) {
        Timber.d("Bus received %s object", o.getClass().toString());
        bus.onNext(o);
    }

    @NotNull
    public static Observable<Object> toObservable() {
        return bus;
    }

    @NotNull
    public static <T> Observable<T> toObservable(Class<T> klass) {
        return toObservable().ofType(klass).cast(klass);
    }

    @NotNull
    public static Observable<Object> toObservable(Class... klasses) {
        Observable<Object> observable = Observable.empty();
        for (Class klass : klasses) {
            observable = observable.mergeWith(bus.ofType(klass));
        }
        return observable;
    }
}
