package com.buddyapps.pdfstudio.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.buddyapps.pdfstudio.PdfStudioFileProvider;

import java.io.File;
import java.util.UUID;

/**
 * Persistent, app-private storage for every file PDF Studio generates.
 * Unlike cache, these files survive cache clearing and restarts.
 */
public final class DocumentFileStore {
    private DocumentFileStore() { }

    public static File getDocumentsDir(Context context) {
        File dir = new File(context.getFilesDir(), "documents");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("No se pudo crear la carpeta de documentos");
        }
        return dir;
    }

    public static File newFile(Context context, String prefix, String extension) {
        String safe = prefix.replaceAll("[^A-Za-z0-9_-]", "_");
        String name = safe + "_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 6) + extension;
        return new File(getDocumentsDir(context), name);
    }

    public static Uri contentUri(Context context, File file) {
        return new Uri.Builder()
                .scheme("content")
                .authority(PdfStudioFileProvider.AUTHORITY)
                .appendPath(file.getName())
                .build();
    }

    public static void grantRead(Context context, Intent intent, Uri uri) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static void grantReadWrite(Context context, Intent intent, Uri uri) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }
}
