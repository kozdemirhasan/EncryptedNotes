package com.kozdemirhasan.encryptednotes.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.User;

import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

import static android.widget.Toast.makeText;

public class UserDatabase {

    private SQLiteDatabase db;
    private final Context context;
    private final UserDBHelper dbhelper;

    //constructer
    public UserDatabase(Context c) {
        context = c;
//Dphelper opjesiyle yeni veritabanı oluşturuluyor.
        dbhelper = new UserDBHelper(context, Sabitler.DATABASE_NAME_USER, null,
                Sabitler.DATABASE_VERSION_USER);
    }

    /*
     * Veritabanını operasyonlara kapatmak
     * için kullandığımız method.
     */
    public void kapat() {
        db.close();
    }

    /*
     * Veritabanını yazma ve okuma için açtığımız method
     * **!**
     * ->yazmak için aç, yazma operasyonu değilse exception ver catch bloğunda okumak için aç
     */
    public void ac() throws SQLiteException {
        try {
            db = dbhelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            Log.v("db exception caught", ex.getMessage());
            db = dbhelper.getReadableDatabase();
        }
    }

    public boolean kullaniciVarmiKontrolEt() {
        boolean x = false;
        try {
            Cursor c = kullaniciGetir();
            if (c.moveToFirst()) {
                do {
                    x = true;
                } while (c.moveToNext());
            }

        } catch (SQLiteException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }

        return x;
    }

    public Cursor kullaniciGetir() {
        Cursor c = db.query(Sabitler.TABLO_KULLANICI, null, null, null, null, null,
                null, null);
        return c;
    }

    public long kullaniciKayit(String password) {
        int x;
        try {
            ContentValues yeniDegerler = new ContentValues();
            yeniDegerler.put(Sabitler.ROW_USER_PASSWORD, password);
            yeniDegerler.put(Sabitler.ROW_USER_GUN, 20);
            yeniDegerler.put(Sabitler.ROW_USER_GUN_DURUM, 0);
            yeniDegerler.put(Sabitler.ROW_USER_TEXTSIZE, 18);
            db.insert(Sabitler.TABLO_KULLANICI, null, yeniDegerler);
            x = 1;
        } catch (SQLiteException ex) {

            Log.v("ekleme isleminde hata !",
                    ex.getMessage());
            //  Toast.makeText(this.context, "Kullanıcı adı daha önce kayıtlı\nBaşka bir kullanıcı adı ile kayıt yapınız", Toast.LENGTH_LONG).show();
            x = -1;

        }
        return x;
    }



    public boolean passwordKonrolEt(String password) {
        Boolean durum = false;
        Cursor c = db.query(Sabitler.TABLO_KULLANICI, null,
                Sabitler.ROW_USER_PASSWORD + " = ? ",
                new String[]{password}, null, null, null);
        //Kullanıcı ismi yoksa hata veriliyor.
        if (c.getCount() < 1) {
            c.close();
            return durum = false;
        } else {
            // c.moveToFirst();
            c.close();
            return durum = true;
        }
    }


    public int parolaDegistir(String eskiParola, String yeniParola) {
        try {

            ContentValues guncelDegerler = new ContentValues();
            guncelDegerler.put(Sabitler.ROW_USER_PASSWORD, new Crypt().encrypt(yeniParola, yeniParola));

            //      return db.update(Sabitler.TABLO_KULLANICI, guncelDegerler, Sabitler.ROW_USER_PASSWORD + "=?", idArray);
            return db.update(Sabitler.TABLO_KULLANICI, guncelDegerler, null, null);


        } catch (SQLiteException ex) {
            return -1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;

        }

    }


    public int gunSayisiSetEt(int gun) {

        try {
            ContentValues guncelDegerler = new ContentValues();
            guncelDegerler.put(Sabitler.ROW_USER_GUN, gun);

            return db.update(Sabitler.TABLO_KULLANICI, guncelDegerler, null, null);

        } catch (SQLiteException ex) {
            return -1;

        }
    }

    public int textSizeSetEt(int textSize) {

        try {
            ContentValues guncelDegerler = new ContentValues();
            guncelDegerler.put(Sabitler.ROW_USER_TEXTSIZE, textSize);

            return db.update(Sabitler.TABLO_KULLANICI, guncelDegerler, null, null);

        } catch (SQLiteException ex) {
            return -1;

        }
    }


    public int silmeDurumTrueYap() {
        try {

            ContentValues guncelDegerler = new ContentValues();
            guncelDegerler.put(Sabitler.ROW_USER_GUN_DURUM, 1);

            return db.update(Sabitler.TABLO_KULLANICI, guncelDegerler, null, null);

        } catch (SQLiteException ex) {
            return -1;

        }
    }

    public int silmeDurumFalseYap() {
        try {

            ContentValues guncelDegerler = new ContentValues();
            guncelDegerler.put(Sabitler.ROW_USER_GUN_DURUM, 0);

            return db.update(Sabitler.TABLO_KULLANICI, guncelDegerler, null, null);

        } catch (SQLiteException ex) {
            return -1;

        }
    }

    public User ayarlar() {
        // HashMap<String, Integer> ayarlar = new HashMap<String, Integer>();
        User ayarlar = new User();
        Cursor c = null;
        try {
       /*     //Sabitler.ROW_USER_ID + " = ? ", new String[]{String.valueOf(idKullanici)}
            c = db.query(Sabitler.TABLO_KULLANICI, new String[]{Sabitler.ROW_USER_GUN,
                            Sabitler.ROW_USER_GUN_DURUM, Sabitler.ROW_USER_TEXTSIZE},
                    null, null, null, null, null);
*/
            c = db.query(Sabitler.TABLO_KULLANICI, null,
                    null, null, null, null, null);

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        if (c.moveToNext()) {
            do {

                ayarlar.setUserId(c.getInt(c.getColumnIndex(Sabitler.KEY_USER_ID)));
                ayarlar.setPassword(c.getString(c.getColumnIndex(Sabitler.ROW_USER_PASSWORD)));
                               ayarlar.setSilmeDurum(c.getInt(c.getColumnIndex(Sabitler.ROW_USER_GUN_DURUM)));
                ayarlar.setSilmeGun(c.getInt(c.getColumnIndex(Sabitler.ROW_USER_GUN)));
                ayarlar.setMetinBoyutu(c.getInt(c.getColumnIndex(Sabitler.ROW_USER_TEXTSIZE)));

            } while (c.moveToNext());
        }
        return ayarlar;
    }


}