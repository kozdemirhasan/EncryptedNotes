package com.kozdemirhasan.encryptednotes.util;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.kozdemirhasan.encryptednotes.custom.ExpListAdapter;
import com.kozdemirhasan.encryptednotes.database.NotesDatabase;
import com.kozdemirhasan.encryptednotes.database.UserDatabase;
import com.kozdemirhasan.encryptednotes.R;

import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.Note;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;
import com.kozdemirhasan.encryptednotes.pojo.SimpleFileDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.kozdemirhasan.encryptednotes.pojo.Sabitler.lastPosition;


public class NotlarActivity extends AppCompatActivity {

    FloatingActionButton fab;
    AlertDialog.Builder alertDialogBuilder;
    EditText searchNoteEdittext;

    ExpandableListView expListView;
    ExpListAdapter adapter;
    ArrayList<String> gruplar;
    ArrayList<Note> notlar;
    HashMap<String, ArrayList<Note>> icerik;
    Note note;

    String eskiPass;
    String yeniPass1;
    String yeniPass2;
    String eskiFakePass;
    String yeniFakePass1;
    String yeniFakePass2;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(30);
        kapatmaUyari();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.notlar);
        setTitle("Encrypted Notes");


        fab = findViewById(R.id.fab);
        searchNoteEdittext = findViewById(R.id.searchNoteEdittext);

        //aranan kelime girildikve sonuclari getir
        searchNoteEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //  search(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(40);
                startActivity(new Intent("android.intent.action.NOTEKLE"));
                finish();

            }
        });


        expListView = (ExpandableListView) findViewById(R.id.exp_list);
        icerik = new HashMap<String, ArrayList<Note>>();

        notlariGetir();

        /*
        son listview konumunu getir
         */
        try {
            //  expListView.onRestoreInstanceState(Sabitler.state);
        } catch (Exception ex) {

        }
        if (lastPosition != -1) {
            expListView.expandGroup(lastPosition);
        }


        //sadece tek grup içeriği açık olabilir
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                // TODO Auto-generated method stub
                if (lastPosition != -1 && lastPosition != groupPosition) {
                    expListView.collapseGroup(lastPosition);
                    // expListView.expandGroup(lastPosition);
                }
                lastPosition = groupPosition;

            }

        });


        kisaUzunTiklama();


    }

    public void search(String searchWort) {
        gruplar.clear();
        notlar.clear();
        icerik.clear();

        SQLiteDatabase database = this.openOrCreateDatabase("mynotesdb", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS notlar (" +
                "_id INTEGER PRIMARY KEY, " +
                "konu VARCHAR , " +
                "icerik VARCHAR, " +
                "grup VARCHAR, " +
                "kayittarihi longtext )");

        Cursor cursor = null;
        try {
            //        cursor = database.rawQuery("SELECT * FROM notlar WHERE icerik =? OR konu =?",
            //               new String[]{new Crypt().encrypt(searchWort, Sabitler.loginPassword),new Crypt().encrypt(searchWort, Sabitler.loginPassword)});

            cursor = database.rawQuery("SELECT * FROM notlar ", null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        int sayac = 0;

        //Curson tipinde gelen notları teker teker dolaşıyoruz
        if (cursor != null) {
            Note searchNot;

            HashSet<String> searchGruplar = new HashSet<>();

            while (cursor.moveToNext()) {
                try {

                    String grup = new Crypt().decrypt(cursor.getString(cursor.getColumnIndex("grup")), Sabitler.loginPassword);
                    String title = new Crypt().decrypt(cursor.getString(cursor.getColumnIndex("konu")), Sabitler.loginPassword);
                    String body = new Crypt().decrypt(cursor.getString(cursor.getColumnIndex("icerik")), Sabitler.loginPassword);

                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String date = cursor.getString(cursor.getColumnIndex("kayittarihi"));
                    //    String body = new Crypt().decrypt(cursor.getString(cursor.getColumnIndex("icerik")), Sabitler.loginPassword);

                    if (title.contains(searchWort) || body.contains(searchWort)) {
                        searchNot = new Note();
                        searchNot.set_id(id);
                        searchNot.setGrup(grup);
                        searchNot.setKonu(title);
                        searchNot.setIcerik(body);
                        searchNot.setKayittarihi(date);

                        notlar.add(searchNot);

                        icerik.put(grup, notlar);

                        gruplar.add(grup);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < gruplar.size(); i++) {
                ArrayList<Note> gecici = new ArrayList<>();
                for (int j = 0; j < notlar.size(); j++) {
                    if (gruplar.get(i).equals(notlar.get(j).getGrup())) {
                        gecici.add(notlar.get(j));
                    }
                }
                icerik.put(gruplar.get(i), gecici); //grubun notlarını set et

            }

            // Toast.makeText(getApplicationContext(), " xxx: " + gruplar.iterator().next(), Toast.LENGTH_SHORT).show();
        }


        adapter = new ExpListAdapter(this, gruplar, icerik);
        expListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }


    private void notlariGetir() {
        //Veritabanından tüm grupları al getir
        NotesDatabase dba = new NotesDatabase(NotlarActivity.this);
        dba.ac();
        try {
            gruplar = dba.tumGruplariGetir();// tekil olarak grup adları getirildi... sıralı halde a-z

        } catch (Exception e) {
            e.printStackTrace();
        }
        int notSay = 0;

        for (int i = 0; i < gruplar.size(); i++) {
            notlar = dba.grubunNotlariniGetir(gruplar.get(i));
            try {
                icerik.put(gruplar.get(i), notlar); //grubun notlarını set et
            } catch (Exception e) {
                e.printStackTrace();
            }
            notSay = notSay + notlar.size();
        }
        dba.kapat();


        if (notSay == 0) {
            Toast.makeText(getApplicationContext(), "No note", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new ExpListAdapter(this, gruplar, icerik);
            expListView.setAdapter(adapter);


        }


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //  menu.setHeaderTitle("Select:");
        menu.add(0, v.getId(), 0, "Show");
        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Delete");

    }

    //nota uzun basıldığında açılan menüde yapılacak işlemler
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Show") {
            notDetayGoruntule(note.get_id());
        } else if (item.getTitle() == "Edit") {
            notGuncelle();
        } else if (item.getTitle() == "Delete") {
            notSil(note.get_id());

        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menunotlar, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Change password")) {
            //change password progress
            eskiPass = null;
            yeniPass1 = null;
            yeniPass2 = null;

            parolaDegistir();

        }else if (item.getTitle().equals("Back up data")) {

            //  chooseImage();
            backUpData();

        } else if (item.getTitle().equals("Restore from backup")) {

            restoreFromBackup();

        } else if (item.getTitle().equals("Update all dates")) {

            tarihBilgisiAl();

        } else if (item.getTitle().equals("Settings")) {
            //ayarlar sayfasına git
            Intent i = new Intent(NotlarActivity.this, AyarlarActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void backUpData() {
        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite()) {
            /////////////////////////////////////////////////////////////////////////////////////////////////
            //Create FileSaveDialog and register a callback
            /////////////////////////////////////////////////////////////////////////////////////////////////
            SimpleFileDialog FileSaveDialog = new SimpleFileDialog(NotlarActivity.this, "FileSave",
                    new SimpleFileDialog.SimpleFileDialogListener() {
                        @Override
                        public void onChosenDir(String chosenDir) {
                            // The code in this function will be executed when the dialog OK button is pushed

                            String m_chosen = chosenDir;

                            backUp(m_chosen, Sabitler.DATABASE_NAME_NOTES);
                        }
                    });

            //You can change the default filename using the public variable "Default_File_Name"

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
           String  tarih = dateFormat.format(System.currentTimeMillis());

            FileSaveDialog.Default_File_Name = "MyNotes_" + tarih;
         //   FileSaveDialog.Default_File_Name = "MyNotes_" + tahihBilgisiniGetir();
            FileSaveDialog.chooseFile_or_Dir();

        } else {
            Toast.makeText(NotlarActivity.this,
                    "Before you can make a backup, you must first grant access to the storage in My Notes", Toast.LENGTH_LONG).show();
        }


    }

    public void restoreFromBackup() {
        File sd2 = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if (sd2.canWrite()) {

            if (sd2.canRead() || data.canRead()) {
                /////////////////////////////////////////////////////////////////////////////////////////////////
                //Create FileOpenDialog and register a callback
                /////////////////////////////////////////////////////////////////////////////////////////////////
                SimpleFileDialog FileOpenDialog = new SimpleFileDialog(NotlarActivity.this, "FileOpen",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                // The code in this function will be executed when the dialog OK button is pushed
                                String m_chosen = chosenDir;
                                restore(m_chosen);


                                //   Toast.makeText(NotlarActivity.this, "Chosen FileOpenDialog File: " +
                                //            m_chosen, Toast.LENGTH_LONG).show();
                            }
                        });

                //You can change the default filename using the public variable "Default_File_Name"
                FileOpenDialog.Default_File_Name = "";
                FileOpenDialog.chooseFile_or_Dir();
            }
        } else {
            Toast.makeText(NotlarActivity.this,
                    "Before you can make a backup, you must first grant access to the storage in My Notes", Toast.LENGTH_LONG).show();
        }


    }


    /*******     local Story erisim izni icin   **********/

    public void chooseImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            //   Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //   startActivityForResult(intent, 2);
            backUpData();

        }

    }


    public void backUp(String m_chosen, String dbName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + getPackageName() + "//databases//" + dbName;
                // String backupDBPath = "backup.db";

                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(m_chosen);

                Log.d("backupDB path", "" + backupDB.getAbsolutePath());

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(), "Backup received.\n" +
                            "(" + m_chosen + ")", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Access to storage is denied", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "An error occurred!!!" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
// izin alininca ne yapilacak
        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //  startActivityForResult(intent, 2);
                backUpData();
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();

       /*     try {
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageData);
                    choosenImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(choosenImage);
                } else {
                    choosenImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                    imageView.setImageBitmap(choosenImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            */
        }


        super.onActivityResult(requestCode, resultCode, data);
    }


    private String tahihBilgisiniGetir() {

        Calendar mcurrentTime = Calendar.getInstance();
        int year = mcurrentTime.get(Calendar.YEAR);//Güncel Yılı alıyoruz
        int month = mcurrentTime.get(Calendar.MONTH);//Güncel Ayı alıyoruz
        int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);//Güncel Günü alıyoruz
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);//Güncel saati aldık
        int minute = mcurrentTime.get(Calendar.MINUTE);//Güncel dakikayı aldık
        int sekond = mcurrentTime.get(Calendar.SECOND);//Güncel saniyeyi aldık

        // return "" + day + month + year + hour + minute + sekond;
        return "" + day + month + year + hour + minute + sekond;

    }

    public void notGuncelle() {

        //Veritabanından notu al getir
        NotesDatabase dba = new NotesDatabase(NotlarActivity.this);
        dba.ac();
        Note notGiden = dba.notGetir(String.valueOf(note.get_id()));//note değişkene atandı
        dba.kapat();

        Crypt crypt = new Crypt();

        Intent i = new Intent(NotlarActivity.this,
                NotGuncelleActivity.class);
        i.putExtra("ID", notGiden.get_id());
        try {
            i.putExtra("KONU", crypt.decrypt(notGiden.getKonu(), Sabitler.loginPassword));
            i.putExtra("GRUP", crypt.decrypt(notGiden.getGrup(), Sabitler.loginPassword));
            i.putExtra("ICERIK", crypt.decrypt(notGiden.getIcerik(), Sabitler.loginPassword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(i);
        finish();

    }

    private void notSil(int ps) {
        final int p = ps;
        final Crypt crypt = new Crypt();
        String konu = null;
        try {
            konu = crypt.decrypt(note.getKonu(), Sabitler.loginPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(
                NotlarActivity.this);

        final String finalKonu = konu;
        builder.setMessage(finalKonu + "\n ... are you sure you want to delete the note on?")
                .setCancelable(true)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(30);
                                NotesDatabase dba = new NotesDatabase(NotlarActivity.this);
                                dba.ac();
                                dba.idIleNotSil(p);
                                dba.kapat();

                                int duration = Toast.LENGTH_SHORT;
                                //Note silindikten sonra silindi olarak bildir.
                                Toast toast = null;
                                try {
                                    toast = Toast.makeText(getApplicationContext(),
                                            finalKonu + " deleted",
                                            duration);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                toast.show();
                                Intent i = new Intent(NotlarActivity.this,
                                        NotlarActivity.class);
                                startActivity(i);
                                finish();

                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(25);
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ayarlar(final View v) {

        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenu().add("Parola değiştir");
        popupMenu.getMenu().add("Fake parola değiştir");
        popupMenu.getMenu().add("Verileri yedekle");
        popupMenu.getMenu().add("Yedekten geri yükle");
        popupMenu.getMenu().add("Tüm tarihleri güncelle");
        popupMenu.getMenu().add("Ayarlar");

        // popupMenu.getMenu().add("user yedekle");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String secilen = item.getTitle().toString();
                if (secilen.toString().equals("Parola değiştir")) {
                    //change password progress
                    eskiPass = null;
                    yeniPass1 = null;
                    yeniPass2 = null;
                    parolaDegistir();

                } else if (secilen.toString().equals("Fake parola değiştir")) {
                    //change fake password
                    eskiFakePass = null;
                    yeniFakePass1 = null;
                    yeniFakePass2 = null;


                } else if (secilen.toString().equals("Verileri yedekle")) {

                    File sd = Environment.getExternalStorageDirectory();
                    if (sd.canWrite()) {
                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        //Create FileSaveDialog and register a callback
                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        SimpleFileDialog FileSaveDialog = new SimpleFileDialog(NotlarActivity.this, "FileSave",
                                new SimpleFileDialog.SimpleFileDialogListener() {
                                    @Override
                                    public void onChosenDir(String chosenDir) {
                                        // The code in this function will be executed when the dialog OK button is pushed
                                        String m_chosen = chosenDir;
                                        backUp(m_chosen, Sabitler.DATABASE_NAME_NOTES);
                                    }
                                });

                        //You can change the default filename using the public variable "Default_File_Name"
                        FileSaveDialog.Default_File_Name = "backup.db";
                        FileSaveDialog.chooseFile_or_Dir();
                    } else {
                        Toast.makeText(NotlarActivity.this,
                                "Yedekleme yapabilmek için önce My Notes programına depolama alanına erişim izni vermelisiniz", Toast.LENGTH_LONG).show();
                    }


                } else if (secilen.toString().equals("Yedekten geri yükle")) {
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();
                    if (sd.canRead() || data.canRead()) {
                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        //Create FileOpenDialog and register a callback
                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        SimpleFileDialog FileOpenDialog = new SimpleFileDialog(NotlarActivity.this, "FileOpen",
                                new SimpleFileDialog.SimpleFileDialogListener() {
                                    @Override
                                    public void onChosenDir(String chosenDir) {
                                        // The code in this function will be executed when the dialog OK button is pushed
                                        String m_chosen = chosenDir;
                                        restore(m_chosen);


                                        //   Toast.makeText(NotlarActivity.this, "Chosen FileOpenDialog File: " +
                                        //            m_chosen, Toast.LENGTH_LONG).show();
                                    }
                                });

                        //You can change the default filename using the public variable "Default_File_Name"
                        FileOpenDialog.Default_File_Name = "";
                        FileOpenDialog.chooseFile_or_Dir();
                    } else {
                        Toast.makeText(NotlarActivity.this,
                                "Yedekten geri yükleme yapabilmek için önce My Notes programına depolama alanına erişim izni vermelisiniz", Toast.LENGTH_LONG).show();
                    }


                } else if (secilen.toString().equals("user yedekle")) {

                    File sd = Environment.getExternalStorageDirectory();
                    if (sd.canWrite()) {
                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        //Create FileSaveDialog and register a callback
                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        SimpleFileDialog FileSaveDialog = new SimpleFileDialog(NotlarActivity.this, "FileSave",
                                new SimpleFileDialog.SimpleFileDialogListener() {
                                    @Override
                                    public void onChosenDir(String chosenDir) {
                                        // The code in this function will be executed when the dialog OK button is pushed
                                        String m_chosen = chosenDir;
                                        backUp(m_chosen, Sabitler.DATABASE_NAME_USER);
                                    }
                                });

                        //You can change the default filename using the public variable "Default_File_Name"
                        FileSaveDialog.Default_File_Name = "user.db";
                        FileSaveDialog.chooseFile_or_Dir();
                    } else {
                        Toast.makeText(NotlarActivity.this,
                                "Yedekleme yapabilmek için önce My Notes programına depolama alanına erişim izni vermelisiniz", Toast.LENGTH_LONG).show();
                    }


                } else if (secilen.toString().equals("Ayarlar")) {
                    //ayarlar sayfasına git
                    Intent i = new Intent(NotlarActivity.this, AyarlarActivity.class);
                    startActivity(i);
                    finish();

                } else if (secilen.toString().equals("Tüm tarihleri güncelle")) {

                    tarihBilgisiAl();

                }
                return true;
            }
        });

    }

    public void tarihBilgisiAl() {

        Calendar mcurrentTime = Calendar.getInstance();
        int year = mcurrentTime.get(Calendar.YEAR);//Güncel Yılı alıyoruz
        int month = mcurrentTime.get(Calendar.MONTH);//Güncel Ayı alıyoruz
        int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);//Güncel Günü alıyoruz

        DatePickerDialog datePicker;//Datepicker objemiz
        datePicker = new DatePickerDialog(NotlarActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                //  tarihTextView.setText( dayOfMonth + "/" + monthOfYear+ "/"+year);//Ayarla butonu tıklandığında textview'a yazdırıyoruz
                Calendar mcurrentTime2 = Calendar.getInstance();
                int hour = mcurrentTime2.get(Calendar.HOUR_OF_DAY);//Güncel saati aldık
                int minute = mcurrentTime2.get(Calendar.MINUTE);//Güncel dakikayı aldık

                monthOfYear += 1; //Aylar sıfırdan başladığı için ayı +1 ekliyoruz.
                String secilenDate = dayOfMonth + "/" + monthOfYear + "/" + year + " " + hour + ":" + minute;
                SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = null;
                try {
                    date = dt.parse(secilenDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long milliseconds = date.getTime();

                NotesDatabase db = new NotesDatabase(NotlarActivity.this);
                db.ac();
                int a = db.tumTarihGuncelle(milliseconds);
                db.kapat();
                if (a == -1) {
                    Toast.makeText(getApplicationContext(), "An error occurred...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Updated the date of all notes", Toast.LENGTH_SHORT).show();
                }

                notlariGetir();


            }
        }, year, month, day);//başlarken set edilcek değerlerimizi atıyoruz
        datePicker.setTitle("Select Date");
        datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Settings", datePicker);
        datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", datePicker);

        datePicker.show();
    }


    public void restore(String m_chosen) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + getPackageName() + "//databases//mynotesdb";
                //  String backupDBPath = "backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(m_chosen);


                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(),
                            "Database updated",
                            Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(NotlarActivity.this,
                            NotlarActivity.class);

                    startActivity(i);
                    finish();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Access to storage is denied",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "An error occurred!!!",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void parolaDegistir() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText et1 = new EditText(this);
        final EditText et2 = new EditText(this);
        final EditText et3 = new EditText(this);
        et1.setHint("current password");
        et2.setHint("new password");
        et3.setHint("new password (repeat)");

        //eğer veri girimişse onalrı set ediyoruz alanlara
        et1.setText(eskiPass);
        et2.setText(yeniPass1);
        et3.setText(yeniPass2);


        et1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        et2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et2.setTransformationMethod(PasswordTransformationMethod.getInstance());
        et3.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et3.setTransformationMethod(PasswordTransformationMethod.getInstance());

        layout.addView(et1);
        layout.addView(et2);
        layout.addView(et3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Daha sonra AlerDialog.Builder'ı oluşturuyoruz.
        builder.setTitle("Change Password");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                //parola değiştir
                eskiPass = et1.getText().toString();
                yeniPass1 = et2.getText().toString();
                yeniPass2 = et3.getText().toString();

                boolean passDrm = passwordKonrol(eskiPass);
                Crypt crypt = new Crypt();
                String yeni_pass_md5 = null;
                try {
                    yeni_pass_md5 = new Crypt().encrypt(yeniPass1, yeniPass1);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (yeniPass1.length() < 6) {
                    Toast.makeText(NotlarActivity.this,
                            "Warning...\nPassword must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    parolaDegistir();
                } else if (!yeniPass1.equals(yeniPass2)) {
                    Toast.makeText(NotlarActivity.this,
                            "Warning...\nPassword and Pasword (repeat) must be the same", Toast.LENGTH_SHORT).show();
                    parolaDegistir();
                } else if (passDrm == true && yeniPass1.equals(yeniPass2) &&
                        !yeniPass1.equals("")) {

                    prlDeg(eskiPass, yeniPass1);

                } else {
                    Toast.makeText(NotlarActivity.this,
                            "Check the information and try again", Toast.LENGTH_SHORT).show();
                    parolaDegistir();

                }
            }

        }); // Buttonu ve tıklanma olayını ekledik. İster tıklanma olayına bir şeyler yazarsınız, ister de boş bırakırsınız. Size kalmış.
            // Biz boş bıraktık. Tıklantığında diyalog kapanacak.

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {


            }

        }); // Buttonu ve tıklanma olayını ekledik. İster tıklanma olayına bir şeyler yazarsınız, ister de boş bırakırsınız. Size kalmış. Biz boş bıraktık.
        builder.setView(layout);
        AlertDialog alert = builder.create(); // Daha sonra builder'ı AlertDialog'a aktarıyoruz.
        alert.show();// En sonunda ise AlertDialog'umuzu gösteriyoruz.
//Özelleştirme isteğinize göre satır sayısı artabilir. Ancak temel mantık şu: Bir builder oluşturuyoruz. Ona gerekli eklemeyi ve düzenlemeyi yapıyoruz. Daha sonra bunu AlertDialog'a aktarıyoruz ve show metoduyla gösteriyoruz.


    }

    public void prlDeg(String eskiParola, String yeniParola) {
        int knt = 0;
        // tüm notlari getir
        NotesDatabase ndb = new NotesDatabase(NotlarActivity.this);
        ndb.ac();
        List<Note> butunNotlar = ndb.butunNotlar();
        Crypt crypt = new Crypt();
        Note notYeni;

        try {
            if (butunNotlar.size() > 0) {
                for (Note notGelen : butunNotlar) {
                    //herbir notu tek tek yeni parolaya göre crypt la
                    int id = notGelen.get_id();
                    String grupx = crypt.decrypt(notGelen.getGrup(), eskiParola);//kriptoludan normale çevir
                    String baslikx = crypt.decrypt(notGelen.getKonu(), eskiParola); //kriptoludan normale çevir
                    String icerikx = crypt.decrypt(notGelen.getIcerik(), eskiParola); //kriptoludan normale çevir
                    long tarihx = notGelen.getTrh();

                    String grp = crypt.encrypt(grupx, yeniParola);
                    String bskl = crypt.encrypt(baslikx, yeniParola);
                    String icrk = crypt.encrypt(icerikx, yeniParola);

                    //notları önce decrypt et, sonra yeni parolaile encrtypt et ve vt yaz
                    //(id, konu, içerik, tarih, grup) sıralı
                    notYeni = new Note(id, grp, bskl, icrk, tarihx);
                    ndb.notlariYenidenYaz(notYeni);
                }

                //yeni paolayı vt yaz
                UserDatabase dba = new UserDatabase(NotlarActivity.this);
                dba.ac();
                knt = dba.parolaDegistir(eskiPass, yeniPass1);
                dba.kapat();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(NotlarActivity.this,
                    "Error changing password", Toast.LENGTH_SHORT).show();
        }

        if (knt != -1) {
            Toast.makeText(NotlarActivity.this, "Password changed", Toast.LENGTH_SHORT).show();

            //YENİ PAROLAYI GİRİŞDEKİ DEĞERLERİNE SET ETTİK
            try {
                Sabitler.loginPassword = yeniPass1;
                Sabitler.PASS_MD5 = new Crypt().encrypt(yeniParola, yeniParola);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(NotlarActivity.this, "Error changing password", Toast.LENGTH_SHORT).show();
        }
        ndb.kapat();
    }





    private boolean passwordKonrol(String password) {
        UserDatabase db = new UserDatabase(NotlarActivity.this);
        db.ac();
        Crypt crypt = new Crypt();
        boolean y = false;
        try {
            y = db.passwordKonrolEt(new Crypt().encrypt(password, password));
            db.kapat();
            return y;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    private void kisaUzunTiklama() {

        getExpListView().setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(30);

                return false;
            }

        });

        getExpListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                note = (Note) adapter.getChild(groupPosition, childPosition);
                notDetayGoruntule(note.get_id());

                //  Sabitler.state = expListView.onSaveInstanceState(); //listview pozisyon kaydet
                return false;
            }


        });

        ////////////////////////////////

 /*       //uzun tıklandığında Context menü aç
        getExpListView().setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                registerForContextMenu(expListView);
                return false;
            }

        });
*/

        getExpListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    note = (Note) adapter.getChild(groupPosition, childPosition);
                    registerForContextMenu(expListView);

                    // You now have everything that you would as if this was an OnChildClickListener()
                    // Add your logic here.

                    // Return true as we are handling the event.
                    return false;
                }

                return true;
            }
        });


    }

    public void notDetayGoruntule(int notID) {

        Sabitler.state = expListView.onSaveInstanceState(); //listview pozisyon kaydet

        Intent i = new Intent(NotlarActivity.this,
                NotDetayActivity.class);
        i.putExtra("ID", notID);
        startActivity(i);
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Eğer geri butonuna basılırsa
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(40);
            try {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                //Mesaj Penceresini Yaratalım
                alertDialogBuilder.setTitle("Close My Notes?").setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) { //Eğer evet butonuna basılırsa
                                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(25);
                                dialog.dismiss();
                                finish();
                                System.exit(0);
                                //   android.os.Process.killProcess(android.os.Process.myPid());

                            }

                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() { //Eğer hayır butonuna basılırsa

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(25);
                                // Toast.makeText(getApplicationContext(), "My Notes kapatılmadı", Toast.LENGTH_SHORT).show();
                            }

                        });

                alertDialogBuilder.create().show(); //son olarak alertDialogBuilder'ı oluşturup ekranda görüntületiyoruz.

            } catch (IllegalStateException e) {
                //yapımızı try-catch blogu içerisine aldık
                // hata ihtimaline karşı.
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void kapatmaUyari() {
        try {
            alertDialogBuilder = new AlertDialog.Builder(this);
            //Mesaj Penceresini Yaratalım
            alertDialogBuilder.setTitle("Turn off My Notes?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) { //Eğer evet butonuna basılırsa

                            dialog.dismiss();

                            //  Intent i = new Intent(NotlarActivity.this,
                            //         MainActivity.class);
                            // startActivity(i);
                            finish();
                            System.exit(0);
                            //   android.os.Process.killProcess(android.os.Process.myPid());

                        }

                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() { //Eğer hayır butonuna basılırsa

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Toast.makeText(getApplicationContext(), "My Notes kapatılmadı", Toast.LENGTH_SHORT).show();

                        }

                    });

            alertDialogBuilder.create().show(); //son olarak alertDialogBuilder'ı oluşturup ekranda görüntületiyoruz.

        } catch (IllegalStateException e) {
            //yapımızı try-catch blogu içerisine aldık
            // hata ihtimaline karşı.
            e.printStackTrace();
        }
    }

    public ExpandableListView getExpListView() {
        return expListView;
    }


    class ExportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(NotlarActivity.this);

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {
            File dbFile = new File(Environment.getDataDirectory() + "/data/mynotesdb.db");

            File exportDir = new File(Environment.getExternalStorageDirectory(), "");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName());
            try {
                file.createNewFile();
                this.copyFile(dbFile, file);
                return true;
            } catch (IOException e) {
                Log.e("mypck", e.getMessage(), e);
                return false;
            }
        }
        // can use UI thread here

        @Override
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(NotlarActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NotlarActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {

                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null)
                    outChannel.close();
            }
        }
    }


}


