package com.madeng.wifiqr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class QRContentProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://com.madeng.wifiqr/");
	private static final String TAG = "QRContentProvider";

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return "image/jpeg";
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		try {
			File f = File.createTempFile("wifi_qr_code", ".jpg", getContext().getFilesDir());

			FileOutputStream out = new FileOutputStream(f);
			GenQRFragment.bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
			Log.d(TAG, "compressing jpg");
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
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
