package com.buddyapps.pdfstudio.model;

public class PdfDocumentItem {
    private String id;
    private String title;
    private String path;
    private String uriString;
    private long sizeBytes;
    private int pageCount;
    private long lastOpened;
    private boolean isFavorite;
    private boolean isCreated;

    public PdfDocumentItem(String id, String title, String path, String uriString, long sizeBytes,
                           int pageCount, long lastOpened, boolean isFavorite) {
        this(id, title, path, uriString, sizeBytes, pageCount, lastOpened, isFavorite, false);
    }

    public PdfDocumentItem(String id, String title, String path, String uriString, long sizeBytes,
                           int pageCount, long lastOpened, boolean isFavorite, boolean isCreated) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.uriString = uriString;
        this.sizeBytes = sizeBytes;
        this.pageCount = pageCount;
        this.lastOpened = lastOpened;
        this.isFavorite = isFavorite;
        this.isCreated = isCreated;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPath() { return path; }
    public String getUriString() { return uriString; }
    public long getSizeBytes() { return sizeBytes; }
    public int getPageCount() { return pageCount; }
    public long getLastOpened() { return lastOpened; }
    public boolean isFavorite() { return isFavorite; }
    public boolean isCreated() { return isCreated; }
}
