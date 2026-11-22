package com.buddyapps.pdfstudio.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.View;

import com.buddyapps.pdfstudio.data.DocumentFileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfAnnotationCore {
    private final Context context;

    public PdfAnnotationCore(Context context) {
        this.context = context;
    }

    public static class DrawingCanvasView extends View {
        private Paint paint;
        private Path path;
        private Bitmap canvasBitmap;
        private Canvas drawCanvas;

        public DrawingCanvasView(Context context) {
            super(context);
            init(Color.parseColor("#4F46E5"), 6f, false);
        }

        public void init(int color, float strokeWidth, boolean isHighlighter) {
            path = new Path();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(strokeWidth);

            if (isHighlighter) {
                paint.setAlpha(120); // translucent
                paint.setStrokeWidth(strokeWidth * 3);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w > 0 && h > 0) {
                canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                drawCanvas = new Canvas(canvasBitmap);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (canvasBitmap != null) {
                canvas.drawBitmap(canvasBitmap, 0, 0, null);
            }
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(x, y);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(x, y);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (drawCanvas != null) {
                        drawCanvas.drawPath(path, paint);
                    }
                    path.reset();
                    invalidate();
                    return true;
            }
            return super.onTouchEvent(event);
        }

        public void clear() {
            if (drawCanvas != null) {
                drawCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                invalidate();
            }
        }

        public Bitmap getDrawnBitmap() {
            return canvasBitmap;
        }
    }

    public File bakeAnnotationToPdf(File source, int pageIndex, Bitmap overlay) throws Exception {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(pfd);
        int total = renderer.getPageCount();

        PdfDocument doc = new PdfDocument();

        for (int i = 0; i < total; i++) {
            PdfRenderer.Page page = renderer.openPage(i);
            int w = page.getWidth();
            int h = page.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
            page.close();

            if (i == pageIndex && overlay != null) {
                Bitmap scaledOverlay = Bitmap.createScaledBitmap(overlay, w * 2, h * 2, true);
                canvas.drawBitmap(scaledOverlay, 0, 0, null);
            }

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, i + 1).create();
            PdfDocument.Page docPage = doc.startPage(pageInfo);
            docPage.getCanvas().drawBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 0, 0, null);
            doc.finishPage(docPage);
        }

        renderer.close();
        pfd.close();

        File outFile = DocumentFileStore.newFile(context, "Anotado", ".pdf");
        FileOutputStream fos = new FileOutputStream(outFile);
        doc.writeTo(fos);
        fos.close();
        doc.close();

        return outFile;
    }
}
