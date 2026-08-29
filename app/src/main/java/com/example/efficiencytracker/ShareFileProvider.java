package com.example.efficiencytracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.File;

/** Serves temporary report images to the Android share sheet without exposing file:// URIs. */
public class ShareFileProvider extends ContentProvider {
    private File shareDir() { return new File(requireContext().getCacheDir(), "share"); }

    private File fileFor(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null || name.isEmpty() || name.contains("..") || name.contains("/")) return null;
        File base = shareDir();
        File file = new File(base, name);
        try {
            if (!file.getCanonicalFile().toPath().startsWith(base.getCanonicalFile().toPath())) return null;
        } catch (Exception e) { return null; }
        return file;
    }

    @Override public boolean onCreate() { return true; }
    @Override public String getType(Uri uri) {
        String name = uri == null ? "" : uri.getLastPathSegment();
        if (name != null && name.toLowerCase(java.util.Locale.ROOT).endsWith(".jpg")) return "image/jpeg";
        if (name != null && name.toLowerCase(java.util.Locale.ROOT).endsWith(".jpeg")) return "image/jpeg";
        return "image/png";
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) {
        File file = fileFor(uri);
        if (file == null || !file.isFile()) throw new IllegalArgumentException("File not found");
        try {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        File file = fileFor(uri);
        if (file == null || !file.isFile()) return null;
        MatrixCursor cursor = new MatrixCursor(new String[]{"_display_name", "_size"});
        cursor.addRow(new Object[]{file.getName(), file.length()});
        return cursor;
    }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        File file = fileFor(uri);
        return file != null && file.delete() ? 1 : 0;
    }

    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
}
