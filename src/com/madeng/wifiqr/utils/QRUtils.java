package com.madeng.wifiqr.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.madeng.wifiqr.GenQRFragment;

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
  public static void createQrCode(String content, int qrCodeSize, Bitmap bmpIn, String ssidText, Context context) {
    try {
      Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
      hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

      BitMatrix byteMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

      int matrixWidth = byteMatrix.getWidth();

      Canvas canvas = new Canvas(bmpIn);
      Paint paint = new Paint();
      canvas.drawColor(Color.WHITE);

      paint.setColor(Color.BLACK);
      paint.setStyle(Style.FILL);

      int lastBlackY = -1,
          tenPercent = matrixWidth / 20;

      for (int i = 0; i < matrixWidth; i++) {
        for (int j = 0; j < matrixWidth; j++) {
          if (byteMatrix.get(i, j)) {
            lastBlackY = Math.max(j - tenPercent, lastBlackY);
            canvas.drawPoint(i, j - tenPercent, paint);
          }
        }
      }

      //canvas.drawBitmap(BitmapFactory.decodeStream(context.getAssets().open("wifi-icon.png")), null, new RectF(
      //                  qrCodeSize/2 - qrCodeSize*1/6, qrCodeSize/2 - qrCodeSize*1/6 - tenPercent,
      //                  qrCodeSize/2 + qrCodeSize*1/6, qrCodeSize/2 + qrCodeSize*1/6 - tenPercent), paint);

      paint.setTypeface(GenQRFragment.face);
      float fontSize = 10;
      Rect bounds = new Rect();
      int gap =  qrCodeSize - lastBlackY;
      int margin = (int) (gap / 7f);

      while(true) {
        paint.setTextSize(fontSize);

        paint.getTextBounds(ssidText, 0, ssidText.length(), bounds);

        if (bounds.height() + (margin*2) > gap || bounds.width() + (margin*2) > qrCodeSize)
          break;

        fontSize += 0.1;
      }

      paint.setAntiAlias(true);
      paint.setSubpixelText(true);
      paint.setTextAlign(Paint.Align.CENTER);
      canvas.drawText(ssidText, qrCodeSize / 2f, qrCodeSize - margin - bounds.bottom, paint);

      /*paint.setColor(Color.GREEN);
      canvas.drawLine(0, qrCodeSize - 5 - bounds.bottom, matrixWidth, qrCodeSize - 5 - bounds.bottom, paint);
      canvas.drawLine(0, qrCodeSize - 5 - bounds.bottom  + bounds.top, matrixWidth, qrCodeSize - 5 - bounds.bottom  + bounds.top, paint);
      canvas.drawLine(0, qrCodeSize - 10 - bounds.bottom  + bounds.top, matrixWidth, qrCodeSize - 10 - bounds.bottom  + bounds.top, paint);

      paint.setColor(Color.BLUE);
      canvas.drawLine(0, qrCodeSize, matrixWidth, qrCodeSize, paint);
      canvas.drawLine(0, lastBlackY, matrixWidth, lastBlackY, paint);*/

      //canvas.drawLine(0, lastBlackY, matrixWidth, lastBlackY, paint);
      //canvas.drawLine(0, qrCodeSize - 3 - paint.getFontMetrics().descent, matrixWidth, qrCodeSize - 3 - paint.getFontMetrics().descent, paint);

    } catch (Exception ex) {
      Log.d(TAG, "createQrCode error", ex);
    }
  }
}
