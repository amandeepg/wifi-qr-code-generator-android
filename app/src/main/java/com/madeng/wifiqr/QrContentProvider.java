package com.madeng.wifiqr;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.madeng.wifiqr.utils.QrUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import io.realm.Realm;
import rx.schedulers.Schedulers;

public class QrContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/");
    public static final String MIME_TYPE = "image/png";
    public static final String FILE_EXT = ".png";
    public static final int QR_SIZE = 1024;
    public static final String FILENAME_SUFFIX = "-qr-code";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(@NotNull Uri uri) {
        return MIME_TYPE;
    }

    @Override
    public ParcelFileDescriptor openFile(@NotNull Uri uri, @NotNull String mode) throws FileNotFoundException {
        assert getContext() != null;

        try {
            final QrNetworkInfo info = getNetworkInfoFromUri(uri);
            File f = File.createTempFile(info.getName() + FILENAME_SUFFIX, FILE_EXT, getContext().getFilesDir());

            FileOutputStream out = new FileOutputStream(f);
            final Bitmap bmp = QrUtils
                    .createQrCodeObservable(info, QR_SIZE, getContext())
                    .observeOn(Schedulers.computation())
                    .toBlocking()
                    .single();
            if (bmp == null) {
                throw new IllegalArgumentException();
            }
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

            return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private QrNetworkInfo getNetworkInfoFromUri(@NotNull Uri uri) {
        assert getContext() != null;

        final String lastPathSegment = uri.getLastPathSegment();
        final String ssid = lastPathSegment.substring(0, lastPathSegment.length() - 4);
        return new NetworkInfoDataService(getContext()).networkInfo(ssid).toBlocking().single();
    }

    @Override
    public int delete(@NotNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(@NotNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public Cursor query(@NotNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        assert getContext() != null;

        final List<QrNetworkInfo> infos = Realm.getInstance(getContext()).allObjects(QrNetworkInfo.class);
        final MatrixCursor mc = new MatrixCursor(projection);

        for (QrNetworkInfo info : infos) {
            final Object[] row = new Object[projection.length];
            for (int i = 0; i < projection.length; i++) {
                if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DISPLAY_NAME) == 0) {
                    row[i] = getContext().getString(R.string.app_name);
                } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DATA) == 0) {
                    row[i] = info.getName() + FILE_EXT;
                } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.MIME_TYPE) == 0) {
                    row[i] = MIME_TYPE;
                }
            }
            mc.addRow(row);
        }
        return mc;
    }

    @Override
    public int update(@NotNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
