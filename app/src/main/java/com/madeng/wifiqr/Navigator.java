package com.madeng.wifiqr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

public class Navigator {

    private Navigator() {
        throw new AssertionError("No instances.");
    }

    public static void shareQrCode(@NotNull Activity activity, @NotNull String name) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(QrContentProvider.CONTENT_URI + name + QrContentProvider.FILE_EXT));
        intent.setType(QrContentProvider.MIME_TYPE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(intent, activity.getResources().getString(R.string.share_qr_code)));
    }
}
