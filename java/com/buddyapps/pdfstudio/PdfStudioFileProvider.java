package com.buddyapps.pdfstudio;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.buddyapps.pdfstudio.data.DocumentFileStore;

import java.io.File;

/**
 * Minimal FileProvider implemented with framework classes only (no AndroidX).
 * Exposes only files inside filesDir/documents.
 */
public class PdfStudioFileProvider extends ContentProvider {
    public static final String AUTHORITY = "com.buddyapps.pdfstudio.fileprovider";

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        MATCHER.addURI(AUTHORITY, "*", 1);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws java.io.FileNotFoundException {
        if (MATCHER.match(uri) != 1) throw new java.io.FileNotFoundException("URI no válida: " + uri);
        String name = uri.getLastPathSegment();
        if (name == null || name.contains("/") || name.contains("..")) {
            throw new java.io.FileNotFoundException("Nombre de archivo no válido");
        }
        File root = DocumentFileStore.getDocumentsDir(getContext());
        File file = new File(root, name);
        try {
            if (!file.getCanonicalPath().startsWith(root.getCanonicalPath())) {
                throw new java.io.FileNotFoundException("Ruta fuera del almacenamiento permitido");
            }
            int pfdMode = ParcelFileDescriptor.MODE_READ_ONLY;
            if (mode != null && (mode.contains("w") || mode.contains("rwt"))) {
                pfdMode = ParcelFileDescriptor.MODE_READ_WRITE;
                if (!file.exists()) file.createNewFile();
            }
            return ParcelFileDescriptor.open(file, pfdMode);
        } catch (Exception e) {
            throw new java.io.FileNotFoundException("No se pudo abrir " + name + ": " + e.getMessage());
        }
    }

    @Override
    public String getType(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name != null && name.toLowerCase().endsWith(".pdf")) return "application/pdf";
        if (name != null && (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"))) return "image/jpeg";
        return "application/octet-stream";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context c = getContext();
        if (c == null || MATCHER.match(uri) != 1) return null;
        String name = uri.getLastPathSegment();
        File file = new File(DocumentFileStore.getDocumentsDir(c), name);
        String[] cols = {"_display_name", "_size"};
        MatrixCursor cursor = new MatrixCursor(cols);
        cursor.addRow(new Object[]{name, file.exists() ? file.length() : 0});
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Inserción no soportada");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Borrado no soportado");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Actualización no soportada");
    }
}
