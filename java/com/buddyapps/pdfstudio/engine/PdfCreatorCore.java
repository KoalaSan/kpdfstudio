package com.buddyapps.pdfstudio.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import com.buddyapps.pdfstudio.data.DocumentFileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfCreatorCore {
    private final Context context;

    public PdfCreatorCore(Context context) {
        this.context = context;
    }

    public File createPdfFromText(String title, String content) throws Exception {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // White background
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, 595, 842, paint);

        // Header Banner
        paint.setColor(Color.parseColor("#007ACC"));
        canvas.drawRect(40, 40, 555, 46, paint);

        // Title
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText(title, 40, 85, paint);

        // Timestamp
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        paint.setTextSize(12);
        paint.setColor(Color.GRAY);
        paint.setFakeBoldText(false);
        canvas.drawText("PDF Studio Pro • Generado el " + dateStr, 40, 110, paint);

        paint.setColor(Color.LTGRAY);
        canvas.drawRect(40, 125, 555, 126, paint);

        // Content
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        int y = 160;
        String[] paragraphs = content.split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                y += 18;
                continue;
            }
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (paint.measureText(line + word + " ") > 500) {
                    canvas.drawText(line.toString(), 40, y, paint);
                    y += 20;
                    line = new StringBuilder(word).append(" ");
                    if (y > 800) break;
                } else {
                    line.append(word).append(" ");
                }
            }
            if (line.length() > 0 && y <= 800) {
                canvas.drawText(line.toString(), 40, y, paint);
                y += 20;
            }
            y += 6;
            if (y > 800) break;
        }

        doc.finishPage(page);

        File outFile = DocumentFileStore.newFile(context, "Doc", ".pdf");
        FileOutputStream fos = new FileOutputStream(outFile);
        doc.writeTo(fos);
        fos.close();
        doc.close();

        return outFile;
    }

    public File convertImageToPdf(Uri imageUri) throws Exception {
        InputStream in = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        if (in != null) in.close();

        if (bitmap == null) return null;

        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawColor(Color.WHITE);

        int maxW = 515;
        int maxH = 762;
        float scale = Math.min((float) maxW / bitmap.getWidth(), (float) maxH / bitmap.getHeight());
        int scaledW = (int) (bitmap.getWidth() * scale);
        int scaledH = (int) (bitmap.getHeight() * scale);

        int left = 40 + (maxW - scaledW) / 2;
        int top = 40 + (maxH - scaledH) / 2;

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true);
        canvas.drawBitmap(scaled, left, top, null);
        doc.finishPage(page);

        File outFile = DocumentFileStore.newFile(context, "Escaneo", ".pdf");
        FileOutputStream fos = new FileOutputStream(outFile);
        doc.writeTo(fos);
        fos.close();
        doc.close();

        return outFile;
    }
}
