package com.buddyapps.pdfstudio.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Writes a valid PDF where every page is a JPEG (DCTDecode) image XObject.
 * Using a deliberately controlled JPEG quality produces real size reductions,
 * unlike re-encoding through PdfDocument which stores pages almost losslessly.
 */
public class PdfJpegWriterCore {
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final Charset LATIN = Charset.forName("ISO-8859-1");
    private static final float MAX_RENDER_DIMENSION = 2000f;

    private static class EncodedPage {
        byte[] jpeg;
        int imageWidth;
        int imageHeight;
        int pageWidth;
        int pageHeight;
    }

    /** Renders and writes a compressed copy. Returns the number of pages written. */
    public int write(File source, File output, int quality) throws Exception {
        if (quality < 40 || quality > 95) throw new IllegalArgumentException("Calidad JPEG fuera de rango");

        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(pfd);
        List<EncodedPage> pages = new ArrayList<>();

        try {
            for (int i = 0; i < renderer.getPageCount(); i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int pw = page.getWidth();
                int ph = page.getHeight();
                float scale = Math.min(2.0f, MAX_RENDER_DIMENSION / Math.max(pw, ph));
                if (scale < 0.25f) scale = 0.25f;
                int iw = Math.max(1, Math.round(pw * scale));
                int ih = Math.max(1, Math.round(ph * scale));

                Bitmap bitmap = Bitmap.createBitmap(iw, ih, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, jpegOut)) {
                    throw new IllegalStateException("No se pudo codificar la página " + (i + 1) + " a JPEG");
                }
                bitmap.recycle();

                EncodedPage ep = new EncodedPage();
                ep.jpeg = jpegOut.toByteArray();
                ep.imageWidth = iw;
                ep.imageHeight = ih;
                ep.pageWidth = pw;
                ep.pageHeight = ph;
                pages.add(ep);
            }
        } finally {
            renderer.close();
            pfd.close();
        }

        writePdf(pages, output);
        return pages.size();
    }

    private static void writePdf(List<EncodedPage> pages, File output) throws Exception {
        int totalObjects = 2 + 3 * pages.size();
        byte[][] objects = new byte[totalObjects + 1][];

        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < pages.size(); p++) {
            if (p > 0) kids.append(' ');
            kids.append(3 + 3 * p).append(" 0 R");
        }
        objects[1] = ascii("<< /Type /Catalog /Pages 2 0 R >>");
        objects[2] = ascii("<< /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >>");

        for (int p = 0; p < pages.size(); p++) {
            EncodedPage ep = pages.get(p);
            int pageObj = 3 + 3 * p;
            int contentObj = pageObj + 1;
            int imageObj = pageObj + 2;

            objects[pageObj] = latin("<< /Type /Page /Parent 2 0 R " +
                    "/MediaBox [0 0 " + ep.pageWidth + " " + ep.pageHeight + "] " +
                    "/Resources << /XObject << /Im0 " + imageObj + " 0 R >> /ProcSet [/PDF /ImageC] >> " +
                    "/Contents " + contentObj + " 0 R >>");

            String content = "q\n" + ep.pageWidth + " 0 0 " + ep.pageHeight + " 0 0 cm\n/Im0 Do\nQ\n";
            byte[] contentBytes = latin(content);
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            contentStream.write(latin("<< /Length " + contentBytes.length + " >>\nstream\n"));
            contentStream.write(contentBytes);
            contentStream.write(latin("\nendstream"));
            objects[contentObj] = contentStream.toByteArray();

            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            imageStream.write(latin("<< /Type /XObject /Subtype /Image /Width " + ep.imageWidth +
                    " /Height " + ep.imageHeight + " /ColorSpace /DeviceRGB /BitsPerComponent 8 " +
                    "/Filter /DCTDecode /Length " + ep.jpeg.length + " >>\nstream\n"));
            imageStream.write(ep.jpeg);
            imageStream.write(latin("\nendstream"));
            objects[imageObj] = imageStream.toByteArray();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ascii("%PDF-1.4\n"));
        out.write(new byte[]{(byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, (byte) '\n'});

        long[] offsets = new long[totalObjects + 1];
        for (int i = 1; i <= totalObjects; i++) {
            offsets[i] = out.size();
            out.write(ascii(i + " 0 obj\n"));
            out.write(objects[i]);
            out.write(ascii("\nendobj\n"));
        }

        long xrefOffset = out.size();
        out.write(ascii("xref\n0 " + (totalObjects + 1) + "\n"));
        out.write(ascii("0000000000 65535 f \n"));
        for (int i = 1; i <= totalObjects; i++) {
            out.write(ascii(String.format(Locale.US, "%010d 00000 n \n", offsets[i])));
        }
        out.write(ascii("trailer\n<< /Size " + (totalObjects + 1) +
                " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n"));

        FileOutputStream fos = new FileOutputStream(output);
        try {
            fos.write(out.toByteArray());
        } finally {
            fos.close();
        }
    }

    private static byte[] ascii(String s) { return s.getBytes(ASCII); }
    private static byte[] latin(String s) { return s.getBytes(LATIN); }
}
