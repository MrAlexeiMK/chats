package com.example.chats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQL extends SQLiteOpenHelper {
    SQL(Context context) {
        super(context, "db", null, 1);
    }
    void addNickname(SQLiteDatabase db, String name) {
        ContentValues cv = new ContentValues();
        cv.put("nickname", name);
        long rowID = db.insert("nicknames", null, cv);
    }
    String getNickname(SQLiteDatabase db) {
        Cursor c = db.query("nicknames", null, null, null, null, null, null);
        if(c.moveToLast()) {
            int id = c.getColumnIndex("nickname");
            return c.getString(id);
        }
        return null;
    }
    public void delUser(SQLiteDatabase db) {
        int delCount = db.delete("nicknames", null, null);
        int delCount2 = db.delete("messages", null, null);
    }
    String getMessages(SQLiteDatabase db, String to) {
        Cursor c = db.query("messages", null, null, null, null, null, null);
        if(c.moveToFirst()) {
            do {
                int id = c.getColumnIndex("to_person");
                int id2 = c.getColumnIndex("msg");
                String name = c.getString(id);
                if(name.equals(to)) {
                    String s = c.getString(id2);
                    return s;
                }
            } while(c.moveToNext());
        }
        return "";
    }
    public void setMessage(SQLiteDatabase db, String to, String msg) {
        ContentValues cv = new ContentValues();
        ContentValues cv2 = new ContentValues();
        cv.put("to_person", to);
        cv.put("msg", msg);
        cv2.put("msg", msg);
        try{
            db.insert("messages", null, cv);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        try {
            db.update("messages", cv2, "to_person=?", new String[]{String.valueOf(to)});
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void clearMessages(SQLiteDatabase db, String to) {
        setMessage(db, to, "");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table nicknames (nickname text primary key not null, UNIQUE(nickname));");
        db.execSQL("create table messages (to_person text primary key not null, msg text not null, UNIQUE(to_person));");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}