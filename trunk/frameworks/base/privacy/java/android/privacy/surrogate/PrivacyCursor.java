package android.privacy.surrogate;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import java.util.Map;

/**
 * Dummy database cursor. Used by {@link android.privacy.surrogate.PrivacyContentResolver} 
 * when access should be blocked without crashing the calling application. 
 * For this purpose none of the methods returns null. 
 */
public class PrivacyCursor implements Cursor {

    @Override
    public void abortUpdates() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean commitUpdates() {
        return false;
    }

    @Override
    public boolean commitUpdates(Map<? extends Long, ? extends Map<String, Object>> values) {
        return false;
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public boolean deleteRow() {
        return false;
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        return new byte[0];
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public int getColumnIndex(String columnName) {
        return 0;
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    @Override
    public String[] getColumnNames() {
        return new String[] { "" };
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public double getDouble(int columnIndex) {
        return 0;
    }

    @Override
    public Bundle getExtras() {
        return new Bundle();
    }

    @Override
    public float getFloat(int columnIndex) {
        return 0;
    }

    @Override
    public int getInt(int columnIndex) {
        return 0;
    }

    @Override
    public long getLong(int columnIndex) {
        return 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public short getShort(int columnIndex) {
        return 0;
    }

    @Override
    public String getString(int columnIndex) {
        return "";
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override
    public boolean hasUpdates() {
        return false;
    }

    @Override
    public boolean isAfterLast() {
        return false;
    }

    @Override
    public boolean isBeforeFirst() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isFirst() {
        return false;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean isNull(int columnIndex) {
        return false;
    }

    @Override
    public boolean move(int offset) {
        return false;
    }

    @Override
    public boolean moveToFirst() {
        return false;
    }

    @Override
    public boolean moveToLast() {
        return false;
    }

    @Override
    public boolean moveToNext() {
        return false;
    }

    @Override
    public boolean moveToPosition(int position) {
        return false;
    }

    @Override
    public boolean moveToPrevious() {
        return false;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public boolean requery() {
        return false;
    }

    @Override
    public Bundle respond(Bundle extras) {
        return new Bundle();
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
    }

    @Override
    public boolean supportsUpdates() {
        return false;
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public boolean updateBlob(int columnIndex, byte[] value) {
        return false;
    }

    @Override
    public boolean updateDouble(int columnIndex, double value) {
        return false;
    }

    @Override
    public boolean updateFloat(int columnIndex, float value) {
        return false;
    }

    @Override
    public boolean updateInt(int columnIndex, int value) {
        return false;
    }

    @Override
    public boolean updateLong(int columnIndex, long value) {
        return false;
    }

    @Override
    public boolean updateShort(int columnIndex, short value) {
        return false;
    }

    @Override
    public boolean updateString(int columnIndex, String value) {
        return false;
    }

    @Override
    public boolean updateToNull(int columnIndex) {
        return false;
    }
}