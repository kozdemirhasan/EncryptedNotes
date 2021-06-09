package com.kozdemirhasan.encryptednotes.pojo;

import android.os.Parcelable;

/**
 * Created by Casper on 10.01.2018.
 */

public class Sabitler {

    public static final String DATABASE_NAME_USER = "userdb";
    public static final int DATABASE_VERSION_USER = 1;

    public static final String TABLO_KULLANICI = "kullanici_tablosu";
    public static final String KEY_USER_ID = "_id";
    public static final String ROW_USER_PASSWORD = "password";
    public static final String ROW_USER_GUN_DURUM = "gundurum";
    public static final String ROW_USER_GUN = "gunsayisi";
    public static final String ROW_USER_TEXTSIZE = "textsize";


    public static final String DATABASE_NAME_NOTES = "mynotesdb";
    public static final int DATABASE_VERSION_NOTES = 1;

    public static final String TABLO_NOTES_NAME = "notlar";
    public static final String KEY_NOT_ID = "_id";
    public static final String ROW_NOTE_TITLE = "konu";
    public static final String ROW_NOTE_BODY = "icerik";
    public static final String ROW_NOTE_GROUP = "grup";
    public static final String ROW_NOTE_DATE = "kayittarihi";

    public static Parcelable state=null;

    public static String loginPassword; //kullanıcının girdiği değer buraya yazılıyor
    public static String PASS_MD5;

    public static int yaziBoyutu = 18;
    public static int lastPosition =-1;


}
