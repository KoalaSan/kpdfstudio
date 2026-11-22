package com.buddyapps.pdfstudio.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.buddyapps.pdfstudio.data.DocumentFileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfPageManagerCore {
    private final Context context;

    public PdfPageManagerCore(Context context) {
        this.context = context;
    }

    public File extractPage(File source, int pageIndex) throws Exception {
        return extractPageRange(source, pageIndex, pageIndex);
    }

    public File extractPageRange(File source, int startPage, int endPage) throws Exception {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(pfd);

        int total = renderer.getPageCount();
        if (startPage < 0) startPage = 0;
        if (endPage >= total) endPage = total - 1;
        if (startPage > endPage) {
            renderer.close();
            pfd.close();
            throw new IllegalArgumentException("La página inicial debe ser menor o igual que la final");
        }

        PdfDocument doc = new PdfDocument();
        int pageCounter = 1;

        for (int i = startPage; i <= endPage; i++) {
            PdfRenderer.Page page = renderer.openPage(i);
            int w = page.getWidth();
            int h = page.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
            page.close();

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, pageCounter++).create();
            PdfDocument.Page docPage = doc.startPage(pageInfo);
            docPage.getCanvas().drawBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 0, 0, null);
            doc.finishPage(docPage);
        }

        renderer.close();
        pfd.close();

        File outFile = DocumentFileStore.newFile(context,
                "Extraido_" + (startPage + 1) + "_" + (endPage + 1), ".pdf");
        FileOutputStream fos = new FileOutputStream(outFile);
        doc.writeTo(fos);
        fos.close();
        doc.close();

        return outFile;
    }

    public File deletePage(File source, int pageToDelete) throws Exception {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(pfd);
        int total = renderer.getPageCount();

        if (total <= 1) {
            renderer.close();
            pfd.close();
            throw new IllegalArgumentException("No se puede eliminar la única página del documento");
        }

        PdfDocument doc = new PdfDocument();
        int outPageNum = 1;

        for (int i = 0; i < total; i++) {
            if (i == pageToDelete) continue;

            PdfRenderer.Page page = renderer.openPage(i);
            int w = page.getWidth();
            int h = page.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
            page.close();

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, outPageNum++).create();
            PdfDocument.Page docPage = doc.startPage(pageInfo);
            docPage.getCanvas().drawBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 0, 0, null);
            doc.finishPage(docPage);
        }

        renderer.close();
        pfd.close();

        File outFile = DocumentFileStore.newFile(context, "PaginaEliminada", ".pdf");
        FileOutputStream fos = new FileOutputStream(outFile);
        doc.writeTo(fos);
        fos.close();
        doc.close();

        return outFile;
    }

    public File mergePdfs(List<File> files) throws Exception {
        if (files == null || files.isEmpty()) return null;

        PdfDocument combinedDoc = new PdfDocument();
        int pageCounter = 1;

        for (File f : files) {
            if (!f.exists()) continue;
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(pfd);

            for (int i = 0; i < renderer.getPageCount(); i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int w = page.getWidth();
                int h = page.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                page.close();

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(w, h, pageCounter++).create();
                PdfDocument.Page docPage = combinedDoc.startPage(pageInfo);
                docPage.getCanvas().drawBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), 0, 0, null);
                combinedDoc.finishPage(docPage);
            }

            renderer.close();
            pfd.close();
        }

        File outFile = DocumentFileStore.newFile(context, "Combinado", ".pdf");
        FileOutputStream fos = new FileOutputStream(outFile);
        combinedDoc.writeTo(fos);
        fos.close();
        combinedDoc.close();

        return outFile;
    }

    /**
     * Real compression: rasterises each page into an ARGB_8888 bitmap (the only
     * format PdfRenderer accepts) and writes it as a DCTDecode/JPEG image with a
     * controlled quality, instead of mis-using PdfDocument's near-lossless store.
     * Returns the file plus the written page count via {@link #getLastCompressionPages()}.
     */
    public File compressPdf(File source) throws Exception {
        return compressPdf(source, 82);
    }

    public File compressPdf(File source, int quality) throws Exception {
        File outFile = DocumentFileStore.newFile(context, "Comprimido", ".pdf");
        lastCompressionPages = new PdfJpegWriterCore().write(source, outFile, quality);
        return outFile;
    }

    public int getLastCompressionPages() { return lastCompressionPages; }
    private int lastCompressionPages = 0;
}
