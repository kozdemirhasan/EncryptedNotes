package com.kozdemirhasan.encryptednotes.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.kozdemirhasan.encryptednotes.database.NotesDatabase;
import com.kozdemirhasan.encryptednotes.R;
import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

import java.util.ArrayList;

public class NotGuncelleActivity extends Activity {

    private EditText konuET, icerikET;
    private Button guncelleBTN;
    TextView twGruplar;
    private NotesDatabase dba;
    private int id;
    String secilenGrup = "DEFAULT GROUP";
    private String m_Text = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.notekleguncelle);

//listele sayfasından gelen ID güncellenecek note id’sini temsil ediyor.

        id = Integer.parseInt(getIntent().getExtras().get("ID").toString());

        konuET = (EditText) findViewById(R.id.konuText);
        icerikET = (EditText) findViewById(R.id.icerikText);

        twGruplar = (TextView) findViewById(R.id.twGruplar);


        guncelleBTN = (Button) findViewById(R.id.opButton);
        guncelleBTN.setText("Update");

        konuET.setText(getIntent().getExtras().get("KONU").toString());
        icerikET.setText(getIntent().getExtras().get("ICERIK").toString());
        secilenGrup = getIntent().getExtras().get("GRUP").toString();
        twGruplar.setText(secilenGrup);

        konuET.setTextSize(Sabitler.yaziBoyutu);
        icerikET.setTextSize(Sabitler.yaziBoyutu);



        twGruplar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(30);
                grupSec(v, grupElemanlari());
            }
        });


        guncelleBTN.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(40);
                    if (konuET.getText().length() != 0 && icerikET.getText().length() != 0) {
                        new IslemTask().execute(); //multi thread işlem başladı... note güncelleniyor
                    } else {
                        Toast.makeText(NotGuncelleActivity.this,
                                "'title' and 'body' no empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * Yeni değerlerimizi alıp o ID’deki notu güncelliyoruz.
     */

    public void notGuncelle() {
        Crypt crypt = new Crypt();
        dba = new NotesDatabase(NotGuncelleActivity.this);
        dba.ac();
        try {
            dba.notGuncelle(id, crypt.encrypt(konuET.getText().toString(), Sabitler.loginPassword),
                    crypt.encrypt(icerikET.getText().toString(), Sabitler.loginPassword),
                    crypt.encrypt(secilenGrup, Sabitler.loginPassword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dba.kapat();


    }

    //Eğer geri butonuna basılırsa
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Eğer geri butonuna basılırsa
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(30);
            Intent i = new Intent(NotGuncelleActivity.this,
                    NotlarActivity.class);
            startActivity(i);
            finish();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void grupSec(final View anchor, ArrayList<String> grupItems) {
        Crypt crypt = new Crypt();
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        popupMenu.getMenu().add("DEFAULT GROUP");
        for (int i = 0; i < grupItems.size(); i++) {
            try {
                String grupElemani = crypt.decrypt(grupItems.get(i),Sabitler.loginPassword);
                if (!grupElemani.equals("DEFAULT GROUP")) {
                    try {
                        popupMenu.getMenu().add(grupElemani);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        popupMenu.getMenu().add("(+) NEW GROUP");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                secilenGrup = item.getTitle().toString().trim().toUpperCase();
                if (secilenGrup.toString().equals("(+) NEW GROUP")) {
                    //yeni grup ekleme ekranı gelsin
                    yeniGrupAl();
                } else {
                    twGruplar.setText(secilenGrup);
                }


                return true;
            }
        });

    }

    public ArrayList<String> grupElemanlari() {
        //Veritabanından tüm grupları al getir
        NotesDatabase dba = new NotesDatabase(NotGuncelleActivity.this);
        dba.ac();
        ArrayList<String> grupItems = null;
        try {
            grupItems = dba.tumGruplariGetir();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dba.kapat();
        return grupItems;

    }

    private void yeniGrupAl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NotGuncelleActivity.this);
        builder.setTitle("NEW GROUP");

// Set up the input
        final EditText input = new EditText(NotGuncelleActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString().toUpperCase();
                if (!m_Text.toString().equals("")) {
                    secilenGrup = m_Text;
                    twGruplar.setText(m_Text);
                } else {
                    secilenGrup = "DEFAULT GROUP";
                    twGruplar.setText(secilenGrup);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                secilenGrup = "DEFAULT GROUP";
                twGruplar.setText(secilenGrup);
                dialog.cancel();
            }
        });

        builder.show();
    }


    private class IslemTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(NotGuncelleActivity.this,
                    "Note update",
                    "Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            notGuncelle();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //note güncellendikten sonra konu ve içerik alanını temizle
            //ve Notlar sayfasına git

            konuET.setText("");
            icerikET.setText("");

            Intent i = new Intent(NotGuncelleActivity.this, NotlarActivity.class);
            startActivity(i);
            finish();

            progressDialog.dismiss();
        }

    }
}