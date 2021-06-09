package com.kozdemirhasan.encryptednotes.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kozdemirhasan.encryptednotes.database.NotesDatabase;
import com.kozdemirhasan.encryptednotes.database.UserDatabase;
import com.kozdemirhasan.encryptednotes.R;
import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.Note;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

public class NotDetayActivity extends Activity {
    TextView baslik;
    TextView icerik;
    TextView tarih;
    Note note;
    Button btnSil;
    Button btnDuzenle;
    Button btnNotlaraDon;
    Button btnAyar;
    Button btnGonder;
    String title;
    String body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.notdetay);

        new IslemTask().execute(); //multi thread işlem başladı...

        btnSil = (Button) findViewById(R.id.btnSil);
        btnDuzenle = (Button) findViewById(R.id.btnDuzenle);
        btnNotlaraDon = (Button) findViewById(R.id.btnNotlaraDon);
        btnAyar = (Button) findViewById(R.id.btnAyar);
        btnGonder = (Button) findViewById(R.id.btnGonder);

        baslik = (TextView) findViewById(R.id.txtBaslik);
        icerik = (TextView) findViewById(R.id.txtIcerik);
        tarih = (TextView) findViewById(R.id.txtTarih);

        btnNotlaraDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(40);

                Intent i = new Intent(NotDetayActivity.this,
                        NotlarActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnSil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(40);
                notSil(note.get_id());
            }
        });

        btnDuzenle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(40);
                notGuncelle();
            }
        });

        btnAyar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ayar();
           /*    Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(40);
                if (seekBar.getVisibility() == View.VISIBLE) {
                    seekBar.setVisibility(View.INVISIBLE);
                } else if (seekBar.getVisibility() == View.INVISIBLE) {
                    seekBar.setVisibility(View.VISIBLE);
                    ayar();
                }
*/
            }
        });

        btnGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickWhatsApp(v);
            }
        });

        //  kisaTikla();


    }

    public void onClickWhatsApp(View view) {

        PackageManager pm = getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = title+"\n"+body ;

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or note. If note then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp note Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private void notuGetir() {
        //Veritabanından notu al getir
        NotesDatabase dba = new NotesDatabase(NotDetayActivity.this);
        dba.ac();
        note = dba.notGetir(getIntent().getExtras().get("ID").toString());
        dba.kapat();


    }

    public void ayar() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(NotDetayActivity.this);
        final SeekBar seek = new SeekBar(NotDetayActivity.this);

        seek.setMax(50);
        seek.setProgress((int) Sabitler.yaziBoyutu);

        icerik.setTextSize(Sabitler.yaziBoyutu);
        tarih.setTextSize(Sabitler.yaziBoyutu - 5);

        seek.setKeyProgressIncrement(1);

        popDialog.setTitle("Text size");
        popDialog.setView(seek);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Text boyutlarını değere göre set ediyoruz.
                Sabitler.yaziBoyutu = progress;
                // baslik.setTextSize(textValue);
                icerik.setTextSize(Sabitler.yaziBoyutu);
                tarih.setTextSize(Sabitler.yaziBoyutu - 5);

                //YAZI TİPİ BOYUTUNU VT YAZ
                try {
                    UserDatabase dba = new UserDatabase(NotDetayActivity.this);
                    dba.ac();
                    int drm = dba.textSizeSetEt(progress);

                    dba.kapat();

                } catch (Exception ex) {
                    Toast.makeText(NotDetayActivity.this, "" + ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
        popDialog.create();
        popDialog.show();


/*
   // Listedeki nota uzun süreli tıklama:
        icerik.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final CharSequence[] items = {"Düzenle", "Sil"};

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        NotDetayActivity.this);
                builder.setTitle(note.konu.toString());
                builder.setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                switch (item) {
                                    case 0:
                                        notGuncelle();
                                        break;
                                    case 1:
                                        notSil(note.get_id());
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return true;

            }

        });*/

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
                NotDetayActivity.this);

        final String finalKonu = konu;
        builder.setMessage(finalKonu + "\n\nAre you sure you want to delete the note on?")
                .setCancelable(true)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(30);
                                NotesDatabase dba = new NotesDatabase(NotDetayActivity.this);
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
                                Intent i = new Intent(NotDetayActivity.this,
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


    public void notGuncelle() {

        //Veritabanından notu al getir
        NotesDatabase dba = new NotesDatabase(NotDetayActivity.this);
        dba.ac();
        Note notGiden = dba.notGetir(String.valueOf(note.get_id()));//note değişkene atandı
        dba.kapat();

        Crypt crypt = new Crypt();

        Intent i = new Intent(NotDetayActivity.this,
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


    //Eğer geri butonuna basılırsa..
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Eğer geri butonuna basılırsa
            //NOTLAR SAYFASINA GİT
            Intent i = new Intent(NotDetayActivity.this,
                    NotlarActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private class IslemTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(NotDetayActivity.this,
                    "Please wait",
                    "Processing...");
        }

        @Override
        protected Void doInBackground(String... params) {
            notuGetir(); //vt dan verileri getir
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //ekrana bas

            if (note != null) {
                Crypt crypt = new Crypt();
                try {
                    title= crypt.decrypt(note.getKonu().toString(), Sabitler.loginPassword);
                    baslik.setText(title);

                    body =crypt.decrypt(note.getIcerik().toString(), Sabitler.loginPassword);
                    icerik.setText(body);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                tarih.setText(note.getKayittarihi().toString());

                icerik.setTextSize(Sabitler.yaziBoyutu);
                tarih.setTextSize(Sabitler.yaziBoyutu - 4);
            }

            progressDialog.dismiss();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_not_detail, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
