package com.kozdemirhasan.encryptednotes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kozdemirhasan.encryptednotes.pojo.Sabitler;


public class UserDBHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE_USER = "create table " + Sabitler.TABLO_KULLANICI
            + " (" + Sabitler.KEY_USER_ID + " integer primary key autoincrement, "
            + Sabitler.ROW_USER_PASSWORD + " text not null, "
            + Sabitler.ROW_USER_GUN_DURUM + " integer not null,"
            + Sabitler.ROW_USER_GUN + " integer not null,"
            + Sabitler.ROW_USER_TEXTSIZE+ " integer not null);";

    public UserDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("NotesDBHelper..", "Tablolar oluşturuyor…");
        try {
// db.execSQL(“drop table if exists”+Sabitler.TABLO);
            db.execSQL(CREATE_TABLE_USER);

        } catch (SQLiteException ex) {
            Log.v("Tablo olusturma hatasi ", ex.getMessage());

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Upgrade islemi", "Tum veriler silinecek !");
/*
* Yenisi geldiğinde eski tablodaki tüm veriler silinecek ve
* tablo yeniden oluşturulacak.
*/
        db.execSQL("drop table if exists " + Sabitler.TABLO_KULLANICI);
        onCreate(db);
    }
}