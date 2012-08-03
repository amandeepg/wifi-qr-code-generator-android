package com.madeng.wifiqr.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class QRUtils {

  private static final String TAG = "QRUtils";

  /**
   * Call this method to create a QR-code image.
   *
   * @param content    The string that should be encoded with the QR-code.
   * @param qrCodeSize The QR-code must be quadratic. So this is the number of pixel
   *                   in width and height.
   */
  public static Bitmap createQrCode(String content, int qrCodeSize) {
    try {
      Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
      hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

      int matrixWidth = byteMatrix.getWidth();

      Bitmap image = Bitmap.createBitmap(matrixWidth, matrixWidth, Config.ARGB_8888);

      Log.d(TAG, "matrixWidth = " + matrixWidth);

      Canvas canvas = new Canvas(image);
      Paint paint = new Paint();
      canvas.drawColor(Color.WHITE);

      paint.setColor(Color.BLACK);
      paint.setStyle(Style.FILL);

      for (int i = 0; i < matrixWidth; i++) {
        for (int j = 0; j < matrixWidth; j++) {
          if (byteMatrix.get(i, j))
            canvas.drawRect(i, j, i + 1, j + 1, paint);
        }
      }

      return image;
    } catch (Exception ex) {
      Log.d(TAG, "createQrCode error", ex);
      return null;
    }
  }
}
