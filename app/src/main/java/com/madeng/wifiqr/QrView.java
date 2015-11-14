package com.madeng.wifiqr;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jakewharton.rxbinding.view.RxView;
import com.madeng.wifiqr.utils.QrUtils;
import com.madeng.wifiqr.utils.rx.RxBus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QrView extends RelativeLayout {

    @Nullable QrNetworkInfo mShownQrNetworkInfo;
    @State @Nullable ParcelableQrNetworkInfo mShownParelableQrNetworkInfo;

    @Bind(R.id.qr_holder) CardView qrHolder;
    @Bind(R.id.qr_imageview) ImageView qrView;
    @Bind(R.id.fab_menu) FloatingActionMenu fabMenu;
    @Bind(R.id.save_item) FloatingActionButton saveItem;
    @Bind(R.id.share_item) FloatingActionButton shareItem;

    public QrView(Context context) {
        super(context);
        init(context);
    }

    public QrView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QrView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public QrView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mShownQrNetworkInfo != null) {
            showQrCode(mShownQrNetworkInfo).subscribe();
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.qr_view, this, true);
        ButterKnife.bind(this);

        RxView.clicks(saveItem).cache()
                .doOnNext(e -> fabMenu.close(true))
                .subscribe(e -> RxBus.send(new SaveNetworkTask(mShownQrNetworkInfo)));

        RxView.clicks(shareItem).cache()
                .doOnNext(e -> fabMenu.close(true))
                .subscribe(e -> RxBus.send(new ShareNetworkTask(mShownQrNetworkInfo)));
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mShownQrNetworkInfo != null) {
            mShownParelableQrNetworkInfo = new ParcelableQrNetworkInfo(mShownQrNetworkInfo);
        }
        return Icepick.saveInstanceState(this, super.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(Icepick.restoreInstanceState(this, state));
        if (mShownParelableQrNetworkInfo != null) {
            mShownQrNetworkInfo = new QrNetworkInfo(mShownParelableQrNetworkInfo);
        }
    }

    private void showQrCode(@NotNull final Bitmap bmp) {
        qrView.setImageBitmap(bmp);
    }

    public Observable<QrNetworkInfo> showQrCode(@NotNull final QrNetworkInfo info) {
        mShownQrNetworkInfo = info;
        return QrUtils.createQrCodeObservable(info, Math.min(qrHolder.getMeasuredWidth(), qrHolder.getMeasuredHeight()), getContext())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::showQrCode)
                .map(bmp -> info);
    }
}
