package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Favourites.db";
    public static final String TABLE_NAME = "streamer_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "URL";
    public static final String COL_4 = "TIMES_WATCHED";
    public static final String COL_5 = "LOGO_URL";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,URL TEXT,TIMES_WATCHED INTEGER, LOGO_URL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    //Insert data into database
    public boolean insertData(String name, String url, String times_watched, String logo_url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, url);
        contentValues.put(COL_4, times_watched);
        contentValues.put(COL_5, logo_url);
        //Insert data, if there is a duplicate, replace it
        long result = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        //If no results
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public boolean updateData(String id, String name, String url, String times_watched, String logo_url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, url);
        contentValues.put(COL_4, times_watched);
        contentValues.put(COL_5, logo_url);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[] { id });
        return true;
    }

    public void updateTimesViewed(Cursor res) {
        int timesWatched = Integer.parseInt(res.getString(3));
        timesWatched++;
        updateData(res.getString(0), res.getString(1), res.getString(2), Integer.toString(timesWatched), res.getString(4));
    }


    public Integer deleteData (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "NAME = ?", new String[] {name});
    }

    //Query string in database and return cursor
    public Cursor getNameMatches(String query) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery("SELECT *, name FROM " + TABLE_NAME + " WHERE name = ?", new String[] {query});
    }
}

