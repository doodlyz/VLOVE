package com.doodlyz.vlove.databases.helper;

import android.database.Cursor;

public interface BuilderPattern<T> {
    void set(Cursor cursor);
    T build();
}
