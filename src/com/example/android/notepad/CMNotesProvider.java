/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;

import com.example.android.notepad.NotePad.Notes;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import me.cloudmine.api.CMAdapter;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class CMNotesProvider extends ContentProvider {

    private static final String TAG = "NotePadProvider";

    private static HashMap<String, String> sNotesProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final int LIVE_FOLDER_NOTES = 3;

    private static final UriMatcher sUriMatcher;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	
    	String[] keys = null;

        switch (sUriMatcher.match(uri)) {
        case NOTES:
            //qb.setProjectionMap(sNotesProjectionMap);
            break;

        case NOTE_ID:
            //qb.setProjectionMap(sNotesProjectionMap);
            //qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1));
        	keys = new String[]{uri.getPathSegments().get(1)};
            break;

        case LIVE_FOLDER_NOTES:
            //qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        //SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        
        CMAdapter cmadapter = new CMAdapter();
        JSONObject objects = cmadapter.getValues(keys);
        MatrixCursor c = new MatrixCursor(projection);
        
        // objects is a map of key => value mappings, where key is the _id
        Iterator<String> note_ids = objects.keys();
        while(note_ids.hasNext()){
        	String id = note_ids.next();
    		RowBuilder row = c.newRow();
        	for(String field: projection){
        		if(field.equals(Notes._ID)){
        			row.add(id);
        		} else {
        			String val = "bad entry";
        			try {
        				val = objects.getJSONObject(id).getString(field);
            		} catch (JSONException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		row.add(val);
        		}
        	}
        }
        
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case NOTES:
        case LIVE_FOLDER_NOTES:
            return Notes.CONTENT_TYPE;

        case NOTE_ID:
            return Notes.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {  	
        // Validate the requested uri
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(NotePad.Notes.CREATED_DATE) == false) {
            values.put(NotePad.Notes.CREATED_DATE, now);
        }

        if (values.containsKey(NotePad.Notes.MODIFIED_DATE) == false) {
            values.put(NotePad.Notes.MODIFIED_DATE, now);
        }

        if (values.containsKey(NotePad.Notes.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(NotePad.Notes.TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(NotePad.Notes.NOTE) == false) {
            values.put(NotePad.Notes.NOTE, "");
        }
        
        CMAdapter cmadapter = new CMAdapter();
        // for the moment, use time for the key
        String key = System.currentTimeMillis() + "";
        String new_key = cmadapter.setValue(key, values);
		if(new_key != null){
			System.out.println("Set key: " + key + ", got key: " + new_key);
			Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, Long.parseLong(new_key));
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

//        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        long rowId = db.insert(NOTES_TABLE_NAME, Notes.NOTE, values);
//        if (rowId > 0) {
//      Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, rowId);
//            getContext().getContentResolver().notifyChange(noteUri, null);
//      return noteUri;
//        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
//        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        CMAdapter cmAdapter = new CMAdapter();
        
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            //count = db.delete(NOTES_TABLE_NAME, where, whereArgs);
        	count = 0;
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            //count = db.delete(NOTES_TABLE_NAME, Notes._ID + "=" + noteId
            //       + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            cmAdapter.deleteKeys(new String[]{ noteId });
            count = 1;
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
//        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        CMAdapter cmAdapter = new CMAdapter();
        
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            //count = db.update(NOTES_TABLE_NAME, values, where, whereArgs);
        	count = 0;
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            //count = db.update(NOTES_TABLE_NAME, values, Notes._ID + "=" + noteId
            //        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            cmAdapter.setValue(noteId, values);
            count = 1;
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);
        sUriMatcher.addURI(NotePad.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);

        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put(Notes._ID, Notes._ID);
        sNotesProjectionMap.put(Notes.TITLE, Notes.TITLE);
        sNotesProjectionMap.put(Notes.NOTE, Notes.NOTE);
        sNotesProjectionMap.put(Notes.CREATED_DATE, Notes.CREATED_DATE);
        sNotesProjectionMap.put(Notes.MODIFIED_DATE, Notes.MODIFIED_DATE);

        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, Notes._ID + " AS " +
                LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, Notes.TITLE + " AS " +
                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }
}
