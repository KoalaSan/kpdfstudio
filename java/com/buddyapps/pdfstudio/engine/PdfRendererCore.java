package com.buddyapps.pdfstudio.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

import java.io.File;

public class PdfRendererCore {
    private final Context context;
    private ParcelFileDescriptor pfd;
    private PdfRenderer renderer;
    private PdfRenderer.Page activePage;
    private int activePageIndex = 0;
    private float zoom = 1.0f;
    private boolean nightMode = false;
    private int rotationDegrees = 0; // 0, 90, 180, 270
    private int pageWidth = 0;
    private int pageHeight = 0;

    public PdfRendererCore(Context context) {
        this.context = context;
    }

    public boolean open(File file) {
        close();
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(pfd);
            activePageIndex = 0;
            rotationDegrees = 0;
            return true;
        } catch (Exception e) {
            close();
            return false;
        }
    }

    public boolean open(ParcelFileDescriptor descriptor) {
        close();
        try {
            this.pfd = descriptor;
            this.renderer = new PdfRenderer(descriptor);
            this.activePageIndex = 0;
            this.rotationDegrees = 0;
            return true;
        } catch (Exception e) {
            close();
            return false;
        }
    }

    public int getPageCount() {
        return (renderer != null) ? renderer.getPageCount() : 0;
    }

    public int getActivePageIndex() {
        return activePageIndex;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        if (zoom >= 0.5f && zoom <= 3.5f) {
            this.zoom = zoom;
        }
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }

    public int getRotationDegrees() {
        return rotationDegrees;
    }

    public int getPageWidth() { return pageWidth; }
    public int getPageHeight() { return pageHeight; }

    public void rotateClockwise() {
        rotationDegrees = (rotationDegrees + 90) % 360;
    }

    public Bitmap renderPage(int index, ImageView targetView) {
        if (renderer == null || index < 0 || index >= renderer.getPageCount()) return null;

        try {
            if (activePage != null) {
                activePage.close();
                activePage = null;
            }

            activePage = renderer.openPage(index);
            activePageIndex = index;

            float density = context.getResources().getDisplayMetrics().density;
            float scale = zoom * (density > 1 ? density * 0.8f : 1f);
            int origW = activePage.getWidth();
            int origH = activePage.getHeight();
            pageWidth = origW;
            pageHeight = origH;

            int targetW = (int) (origW * scale);
            int targetH = (int) (origH * scale);
            if (targetW < 50) targetW = 50;
            if (targetH < 50) targetH = 50;

            Bitmap baseBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(baseBitmap);
            canvas.drawColor(Color.WHITE);
            activePage.render(baseBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            Bitmap finalBitmap = baseBitmap;
            if (rotationDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                finalBitmap = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.getWidth(), baseBitmap.getHeight(), matrix, true);
            }

            if (targetView != null) {
                targetView.setImageBitmap(finalBitmap);
                if (nightMode) {
                    float[] filterMatrix = {
                            -1.0f,  0.0f,  0.0f,  0.0f,  255.0f,
                             0.0f, -1.0f,  0.0f,  0.0f,  255.0f,
                             0.0f,  0.0f, -1.0f,  0.0f,  255.0f,
                             0.0f,  0.0f,  0.0f,  1.0f,    0.0f
                    };
                    targetView.setColorFilter(new ColorMatrixColorFilter(filterMatrix));
                } else {
                    targetView.setColorFilter(null);
                }
            }
            return finalBitmap;
        } catch (OutOfMemoryError oom) {
            this.zoom = 1.0f;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        if (activePage != null) {
            try { activePage.close(); } catch (Exception ignored) {}
            activePage = null;
        }
        if (renderer != null) {
            try { renderer.close(); } catch (Exception ignored) {}
            renderer = null;
        }
        if (pfd != null) {
            try { pfd.close(); } catch (Exception ignored) {}
            pfd = null;
        }
    }
}
