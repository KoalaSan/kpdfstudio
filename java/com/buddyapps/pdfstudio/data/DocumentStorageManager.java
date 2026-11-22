package com.buddyapps.pdfstudio.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import com.buddyapps.pdfstudio.model.PdfDocumentItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentStorageManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "pdf_studio_v2.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_DOCS = "documents";
    private static final String TABLE_BOOKMARKS = "bookmarks";

    private final Context context;

    public DocumentStorageManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_DOCS + " (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "path TEXT UNIQUE, " +
                "uri TEXT, " +
                "size INTEGER, " +
                "page_count INTEGER, " +
                "last_opened INTEGER, " +
                "is_favorite INTEGER, " +
                "is_created INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "doc_path TEXT, " +
                "page_index INTEGER, " +
                "title TEXT, " +
                "created_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // v2 adds the "created" flag without discarding the user's existing documents.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_DOCS + " ADD COLUMN is_created INTEGER DEFAULT 0");
        }
    }

    public synchronized void registerDocument(String title, String path, String uri, long size, int pageCount) {
        registerDocument(title, path, uri, size, pageCount, false);
    }

    public synchronized void registerDocument(String title, String path, String uri, long size, int pageCount, boolean created) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", UUID.randomUUID().toString());
        cv.put("title", title);
        cv.put("path", path);
        cv.put("uri", uri);
        cv.put("size", size);
        cv.put("page_count", pageCount);
        cv.put("last_opened", System.currentTimeMillis());
        cv.put("is_favorite", 0);
        cv.put("is_created", created ? 1 : 0);
        db.insertWithOnConflict(TABLE_DOCS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized List<PdfDocumentItem> getRecentDocuments() {
        List<PdfDocumentItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_DOCS, null, null, null, null, null, "last_opened DESC", "30");
        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndexOrThrow("id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                String path = c.getString(c.getColumnIndexOrThrow("path"));
                String uri = c.getString(c.getColumnIndexOrThrow("uri"));
                long size = c.getLong(c.getColumnIndexOrThrow("size"));
                int pages = c.getInt(c.getColumnIndexOrThrow("page_count"));
                long time = c.getLong(c.getColumnIndexOrThrow("last_opened"));
                boolean fav = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1;
                int createdIdx = c.getColumnIndex("is_created");
                boolean created = createdIdx >= 0 && c.getInt(createdIdx) == 1;
                list.add(new PdfDocumentItem(id, title, path, uri, size, pages, time, fav, created));
            }
            c.close();
        }
        return list;
    }

    public synchronized void toggleFavorite(String id, boolean currentStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_favorite", currentStatus ? 0 : 1);
        db.update(TABLE_DOCS, cv, "id = ?", new String[]{id});
    }

    public synchronized void addBookmark(String docPath, int pageIndex, String title) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("doc_path", docPath);
        cv.put("page_index", pageIndex);
        cv.put("title", title);
        cv.put("created_at", System.currentTimeMillis());
        db.insert(TABLE_BOOKMARKS, null, cv);
    }

    public synchronized void removeBookmark(String docPath, int pageIndex) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_BOOKMARKS, "doc_path = ? AND page_index = ?", new String[]{docPath, String.valueOf(pageIndex)});
    }

    public synchronized boolean isBookmarked(String docPath, int pageIndex) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BOOKMARKS, new String[]{"id"}, "doc_path = ? AND page_index = ?", new String[]{docPath, String.valueOf(pageIndex)}, null, null, null);
        boolean exists = (c != null && c.getCount() > 0);
        if (c != null) c.close();
        return exists;
    }

    public synchronized List<Integer> getBookmarks(String docPath) {
        List<Integer> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BOOKMARKS, new String[]{"page_index"}, "doc_path = ?", new String[]{docPath}, null, null, "page_index ASC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(c.getInt(0));
            }
            c.close();
        }
        return list;
    }

    public File importUriToStorage(Uri uri, String customName) {
        try {
            String prefix = (customName != null && !customName.isEmpty())
                    ? customName.replaceAll("\\.pdf$", "") : "imported";
            File dest = DocumentFileStore.newFile(context, prefix, ".pdf");
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return null;
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[16384];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return dest;
        } catch (Exception e) {
            return null;
        }
    }
}
