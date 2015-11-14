package com.madeng.wifiqr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.madeng.wifiqr.App;
import com.madeng.wifiqr.QrNetworkInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import rx.Observable;
import timber.log.Timber;

public class QrUtils {

    private static Typeface sQrCodeTypeface;

    private QrUtils() {
        throw new AssertionError("No instances.");
    }

    @NotNull
    private static String getQrCodeTextForWifi(@NotNull QrNetworkInfo data) {
        if (data.getAuth() == QrNetworkInfo.AUTH_WPA || data.getAuth() == QrNetworkInfo.AUTH_WEP) {
            final String authText = data.getAuth() == QrNetworkInfo.AUTH_WPA ? "WPA" : "WEP";
            return "WIFI:T:" + authText + ";S:" + data.getName() + ";P:" + data.getPassword() + ";;";
        } else if (data.getAuth() == QrNetworkInfo.AUTH_NONE) {
            return "WIFI:T:" + "nopass" + ";S:" + data.getName() + ";P:" + "nopass" + ";;";
        } else {
            throw new IllegalArgumentException("auth must be one of WPA, WEP, or NONE");
        }
    }

    /**
     * Call this method to create a QR-code image.
     *
     * @param content    The string that should be encoded with the QR-code.
     * @param qrCodeSize The QR-code must be quadratic. So this is the number of pixel
     */
    public static Bitmap createQrCode(@NotNull final String content,
                                      final int qrCodeSize,
                                      @NotNull final String ssidText,
                                      @NotNull final Typeface typeface) {
        try {
            if (qrCodeSize <= 0) {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }

            final int textHeight = qrCodeSize / 8;
            final int actualQrSize = qrCodeSize - textHeight;

            final Bitmap qrBmp = new QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE, actualQrSize, actualQrSize, ErrorCorrectionLevel.H);
            final Bitmap combinedBmp = Bitmap.createBitmap(qrCodeSize, qrCodeSize, Bitmap.Config.ARGB_8888);

            final Canvas canvas = new Canvas(combinedBmp);
            final Paint paint = new Paint();
            canvas.drawColor(Color.TRANSPARENT);

            canvas.drawBitmap(qrBmp, textHeight / 2f, 0, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            paint.setTypeface(typeface);
            float fontSize = 10;
            final Rect bounds = new Rect();
            final int margin = textHeight / 8;

            while (true) {
                paint.setTextSize(fontSize);

                paint.getTextBounds(ssidText, 0, ssidText.length(), bounds);

                if (bounds.height() + (margin * 2) > textHeight || bounds.width() + (margin * 2) > qrCodeSize) {
                    break;
                }

                fontSize += 0.1;
            }

            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(ssidText, qrCodeSize / 2f, actualQrSize + textHeight / 2 + margin, paint);

            return combinedBmp;
        } catch (WriterException e) {
            Timber.d("createQrCode error %s", e);
            return null;
        }
    }

    @NotNull
    public static Observable<Bitmap> createQrCodeObservable(@NotNull final QrNetworkInfo info,
                                                            final int qrSize,
                                                            @NotNull final Context context) {
        return Observable.defer(() -> Observable.just(createQrCode(info, qrSize, context)));
    }

    @Nullable
    private static Bitmap createQrCode(@NotNull final QrNetworkInfo info,
                                       final int qrSize,
                                       @NotNull final Context context) {
        return createQrCode(getQrCodeTextForWifi(info), info.getName(), qrSize, context);
    }

    @Nullable
    private static Bitmap createQrCode(@NotNull final String getQrCodeContent,
                                       @NotNull final String ssid,
                                       final int qrSize,
                                       @NotNull final Context context) {
        if (sQrCodeTypeface == null) {
            sQrCodeTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        }

        final long start = System.currentTimeMillis();
        final Bitmap bmp = createQrCode(getQrCodeContent, qrSize, ssid, sQrCodeTypeface);
        final long duration = System.currentTimeMillis() - start;
        Timber.d("createQrCode time: %s ms on %s", duration, Thread.currentThread());
        sendAnalyticsQrGenerateTiming(context, duration);
        return bmp;
    }

    private static void sendAnalyticsQrGenerateTiming(@NotNull Context context, long duration) {
        App.getTracker(context).send(new HitBuilders.TimingBuilder()
                .setCategory("QrCode")
                .setVariable("GenerateTime")
                .setValue(duration)
                .build());
        App.getAnswers(context).logCustom(new CustomEvent("QrCode GenerateTime").putCustomAttribute("time", duration));
    }
}
