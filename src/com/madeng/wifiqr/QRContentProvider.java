package com.madeng.wifiqr;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class QRContentProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://com.madeng.wifiqr/");
	private static final String TAG = "QRContentProvider";

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return "image/png";
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uriNULL, String modeNULL) throws FileNotFoundException {
		try {
			File f = File.createTempFile(GenQRFragment.ssidName + "-code", ".png", getContext().getFilesDir());

			FileOutputStream out = new FileOutputStream(f);
			GenQRFragment.bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
			Log.d(TAG, "compressing png");
			out.close();

			return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor mc = new MatrixCursor(new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE, "title"});
        mc.addRow(new String[]{GenQRFragment.ssidName + "-QR-Code.png", "image/png", "Wifi QR Code"});
        return mc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
