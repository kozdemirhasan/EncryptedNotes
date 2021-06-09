package com.kozdemirhasan.encryptednotes.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.Note;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.widget.Toast.*;

public class NotesDatabase {

    private SQLiteDatabase db;
    private final Context context;
    private final NotesDBHelper dbhelper;


    //constructer
    public NotesDatabase(Context c) {
        context = c;
        //Dphelper opjesiyle yeni veritabanı oluşturuluyor.
        dbhelper = new NotesDBHelper(context, Sabitler.DATABASE_NAME_NOTES, null,
                Sabitler.DATABASE_VERSION_NOTES);

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


    public void tumNotlariSil() {
        db.delete(Sabitler.TABLO_NOTES_NAME, null, null);
    }


    //grubun tüm notları al getir
    public ArrayList<Note> grubunNotlariniGetir(String grupAdi) {
        ArrayList<Note> notlar = new ArrayList<Note>();
        Cursor c;
        try {
            c = db.query(Sabitler.TABLO_NOTES_NAME, new String[]{Sabitler.KEY_NOT_ID, Sabitler.ROW_NOTE_TITLE,
                            Sabitler.ROW_NOTE_DATE, Sabitler.ROW_NOTE_GROUP},
                    Sabitler.ROW_NOTE_GROUP + "=? ",
                    new String[]{grupAdi},
                    null, null, Sabitler.ROW_NOTE_DATE + " desc");


        } catch (Exception ex) {
            ex.printStackTrace();
            c = null;
        }

//Curson tipinde gelen notları teker teker dolaşıyoruz
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(Sabitler.KEY_NOT_ID));
                String baslik = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_TITLE));
                String grup = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_GROUP));
                //  String icerik = c.getString(c.getColumnIndex(Sabitler.ROW_NOT_ICERIK));

                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                long trhfark = new Date().getTime() - c.getLong(c.getColumnIndex(Sabitler.ROW_NOTE_DATE));
                String tarih = null;
                long birgun = 24 * 60 * 60 * 1000;
                if (trhfark <= 0) {
                    trhfark = trhfark * (-1);
                }

                if (trhfark < birgun) {
                    SimpleDateFormat bicimAyniGun = new SimpleDateFormat("HH:mm");
                    tarih = bicimAyniGun.format(new Date(c.getLong(c
                            .getColumnIndex(Sabitler.ROW_NOTE_DATE))).getTime());
                } else {
                    SimpleDateFormat bicim = new SimpleDateFormat("dd MMM yy");
                    tarih = bicim.format(new Date(c.getLong(c
                            .getColumnIndex(Sabitler.ROW_NOTE_DATE))).getTime());
                }
                Note note = new Note();
                note.set_id(id);
                note.setKonu(baslik);
                note.setGrup(grup);
                note.setKayittarihi(tarih);
                notlar.add(note);
            }
        }
        return notlar;
    }


    //tüm gruplari al getir, kriptolu metin
    public ArrayList<String> tumGruplariGetir() throws Exception {
        ArrayList<String> gruplar = new ArrayList<String>();
        Cursor c = tumGruplariAlGetir();

//Curson tipinde gelen notları teker teker dolaşıyoruz
        if (c != null) {
            while (c.moveToNext()) {
                String grup = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_GROUP));
                gruplar.add(grup);//kriptolu halde grup adları
            }
        }

        Crypt crypt = new Crypt();
        ArrayList<String> gruplarYeni = new ArrayList<String>();
        for (String grp : gruplar) {
            gruplarYeni.add(crypt.decrypt(grp, Sabitler.loginPassword));//kriptolu grup adlarını çözümlüyoruz
        }
        Collections.sort(gruplarYeni);//çözümlediğimiz grup adlarını a-z sıralıyoruz

        ArrayList<String> gruplarSon = new ArrayList<>();
        for (String grp : gruplarYeni) {
            gruplarSon.add(crypt.encrypt(grp, Sabitler.loginPassword));//a-z sıralanmış grup adları tekrar kriptolanıp yeni gruba atıyoruz
        }


        return gruplarSon;
    }

    public Cursor tumGruplariAlGetir() {
        try {
            //Sabitler.ROW_USER_ID + " = ? ", new String[]{String.valueOf(idKullanici)}
            Cursor c = db.query(true, Sabitler.TABLO_NOTES_NAME,
                    new String[]{Sabitler.ROW_NOTE_GROUP},
                    null, null, null, null
                    , null, null);
            return c;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    //başlangıçta tüm notların id ve tarihlerini al getir
    public List<Note> tumKayitlar() {
        List<Note> notlar = new ArrayList<Note>();
        Cursor c = tumNotlar();

//Curson tipinde gelen notları teker teker dolaşıyoruz
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(Sabitler.KEY_NOT_ID));

                long tarihLong = c.getLong(c.getColumnIndex(Sabitler.ROW_NOTE_DATE));
                Note gecici = new Note(id, tarihLong);
//Veritabanındaki tüm notları birer birer ArrayList’e kaydediyoruz.
                notlar.add(gecici);
            }
        }


        return notlar;

    }

    public Cursor tumNotlar() {
        try {
            //Sabitler.ROW_USER_ID + " = ? ", new String[]{String.valueOf(idKullanici)}
            Cursor c = db.query(Sabitler.TABLO_NOTES_NAME, null, null, null, null, null
                    , null, null);
            return c;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }


    }


    /*
     * Veritabanına not eklediğimiz method.
     * insert Yapısı:
     * —-db.insert(String table, String nullColumnHack, ContentValues icerikDegerleri)
     */
    public long notEkle(String konu, String icerik, String grup) throws Exception {
        try {
            ContentValues yeniDegerler = new ContentValues();
            Crypt crypt = new Crypt();
            yeniDegerler.put(Sabitler.ROW_NOTE_TITLE, crypt.encrypt(konu, Sabitler.loginPassword));
            yeniDegerler.put(Sabitler.ROW_NOTE_BODY, crypt.encrypt(icerik, Sabitler.loginPassword));
            yeniDegerler.put(Sabitler.ROW_NOTE_GROUP, crypt.encrypt(grup, Sabitler.loginPassword));
            yeniDegerler.put(Sabitler.ROW_NOTE_DATE, System.currentTimeMillis());
            return db.insert(Sabitler.TABLO_NOTES_NAME, null, yeniDegerler);

        } catch (SQLiteException ex) {

            Log.v("error in adding",
                    ex.getMessage());

            return -1;
        }
    }

    public void notGuncelle(int id, String konu, String icerik, String grup) {

        ContentValues guncelDegerler = new ContentValues();
        String[] idArray = {String.valueOf(id)};

        guncelDegerler.put(Sabitler.ROW_NOTE_TITLE, konu);
        guncelDegerler.put(Sabitler.ROW_NOTE_BODY, icerik);
        guncelDegerler.put(Sabitler.ROW_NOTE_GROUP, grup);
        guncelDegerler.put(Sabitler.ROW_NOTE_DATE, System.currentTimeMillis());
        db.update(Sabitler.TABLO_NOTES_NAME, guncelDegerler, Sabitler.KEY_NOT_ID + " =? ", idArray);


    }

    public int tumTarihGuncelle(long tarih) {
        ContentValues guncelDegerler = new ContentValues();
        guncelDegerler.put(Sabitler.ROW_NOTE_DATE, tarih);
        try {
            db.update(Sabitler.TABLO_NOTES_NAME, guncelDegerler, null, null);
            return 1;
        } catch (Exception ex) {
            return -1;
        }

    }

    public void idIleNotSil(int id) {
        db.delete(Sabitler.TABLO_NOTES_NAME, Sabitler.KEY_NOT_ID + " =" + id, null);
    }

    public void eskileriSil(List<Integer> idler) {
        for (int i = 0; i < idler.size(); i++) {
            String[] idArray = {String.valueOf(idler.get(i))};
            db.delete(Sabitler.TABLO_NOTES_NAME, Sabitler.KEY_NOT_ID + " = ?", idArray);
        }
    }


    //id ile not getir
    public Note notGetir(String id) {

        Cursor c;
        try {
            //Sabitler.ROW_USER_ID + " = ? ", new String[]{String.valueOf(idKullanici)}
            c = db.query(Sabitler.TABLO_NOTES_NAME, null, Sabitler.KEY_NOT_ID + " = ? ",
                    new String[]{id}, null
                    , null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

//Curson tipinde gelen notları teker teker dolaşıyoruz

        if (c.moveToNext()) {
            int idx = c.getInt(c.getColumnIndex(Sabitler.KEY_NOT_ID));
            String baslik = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_TITLE));
            String icerik = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_BODY));
            String grup = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_GROUP));
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            long trhfark = new Date().getTime() - c.getLong(c.getColumnIndex(Sabitler.ROW_NOTE_DATE));
            String tarih;
            if (trhfark < 24 * 60 * 60 * 1000) {
                SimpleDateFormat bicimAyniGun = new SimpleDateFormat("HH:mm:ss");
                tarih = bicimAyniGun.format(new Date(c.getLong(c
                        .getColumnIndex(Sabitler.ROW_NOTE_DATE))).getTime());
            } else {
                SimpleDateFormat bicim = new SimpleDateFormat("dd-MM-yyyy");
                tarih = bicim.format(new Date(c.getLong(c
                        .getColumnIndex(Sabitler.ROW_NOTE_DATE))).getTime());
            }

            Note note = new Note(idx, baslik, icerik, tarih, grup);

            return note;

        } else {
            makeText(null, "An error occurred", LENGTH_SHORT).show();
            return null;
        }


    }


    //parola değiştiriken tüm notların şifrelemesinin değişmesi için tüm notları al getir
    public List<Note> butunNotlar() {
        List<Note> notlar = new ArrayList<Note>();
        Cursor c = tumNotlar2();

//Curson tipinde gelen notları teker teker dolaşıyoruz
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(Sabitler.KEY_NOT_ID));
                String grup = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_GROUP));
                String baslik = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_TITLE));
                String icerik = c.getString(c.getColumnIndex(Sabitler.ROW_NOTE_BODY));
                long tarihLong = c.getLong(c.getColumnIndex(Sabitler.ROW_NOTE_DATE));

                Note gecici = new Note(id, grup, baslik, icerik, tarihLong);

//Veritabanındaki tüm notları birer birer ArrayList’e kaydediyoruz.
                notlar.add(gecici);
            }
        }

        return notlar;

    }

    public Cursor tumNotlar2() {
        try {
            //Sabitler.ROW_USER_ID + " = ? ", new String[]{String.valueOf(idKullanici)}
            Cursor c = db.query(Sabitler.TABLO_NOTES_NAME, null, null, null, null, null
                    , null, null);
            return c;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void notlariYenidenYaz(Note note) {
        ContentValues yeniDegerler = new ContentValues();

        String[] idArray = {String.valueOf(note.get_id())};
        yeniDegerler.put(Sabitler.ROW_NOTE_GROUP, note.getGrup());
        yeniDegerler.put(Sabitler.ROW_NOTE_TITLE, note.getKonu());
        yeniDegerler.put(Sabitler.ROW_NOTE_BODY, note.getIcerik());
        yeniDegerler.put(Sabitler.ROW_NOTE_DATE, note.getTrh());
        db.update(Sabitler.TABLO_NOTES_NAME, yeniDegerler, Sabitler.KEY_NOT_ID + " =?", idArray);
    }

}