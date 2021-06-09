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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.kozdemirhasan.encryptednotes.database.NotesDatabase;
import com.kozdemirhasan.encryptednotes.R;
import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

import java.util.ArrayList;


public class NotEkleActivity extends Activity {

    NotesDatabase dba;
    EditText konuET, icerikET;
    Button ekleBT;
    Button btnGrup;
    TextView twGruplar;
    String secilenGrup = "DEFAULT GROUP";
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.notekleguncelle);

        konuET =  findViewById(R.id.konuText);
        icerikET =  findViewById(R.id.icerikText);
        ekleBT =  findViewById(R.id.opButton);
        twGruplar =  findViewById(R.id.twGruplar);
        twGruplar.setText(secilenGrup);
        konuET.setTextSize(Sabitler.yaziBoyutu);
        icerikET.setTextSize(Sabitler.yaziBoyutu);


        ekleBT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(40);
                try {
                    dba = new NotesDatabase(NotEkleActivity.this);
                    dba.ac();

                    /*
                     * Note ekleme alanları boş ise uyarı ver değilse notu ekle*/

                    if (konuET.getText().length() != 0 && icerikET.getText().length() != 0) {

                        new IslemTask().execute(); //multi thread işlem başladı... note kayıt ediliyor

                    } else {
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(getApplicationContext(),
                                "title and body no empty",
                                duration);
                        toast.show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //


        twGruplar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(30);
                grupSec(v, grupElemanlari());
            }
        });


    }

    private void grupSec(final View anchor, ArrayList<String> grupItems) {
        Crypt crypt = new Crypt();
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        popupMenu.getMenu().add("DEFAULT GROUP");
        for (int i = 0; i < grupItems.size(); i++) {
            try {
                String grupElemani = crypt.decrypt(grupItems.get(i),Sabitler.loginPassword);
                if (!grupElemani.equals("DEFAULT GROUP")) {
                    popupMenu.getMenu().add(grupElemani);
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
                secilenGrup = item.getTitle().toString().trim();
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
        NotesDatabase dba = new NotesDatabase(NotEkleActivity.this);
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

    public void yeniGrupAl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NotEkleActivity.this);
        builder.setTitle("New group add");

// Set up the input
        final EditText input = new EditText(NotEkleActivity.this);
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

    /*
     * EditTextlerden aldığı verileri notEkle methoduna gönderen method
     */
    public void notEkle() {

        try {
            long x = dba.notEkle(konuET.getText().toString(), icerikET.getText()
                    .toString(), secilenGrup);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dba.kapat();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Eğer geri butonuna basılırsa

            Intent i = new Intent(NotEkleActivity.this,
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
            progressDialog = ProgressDialog.show(NotEkleActivity.this,
                    "Note recording",
                    "Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            notEkle();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //note kaydedildikten sonra konu ve içerik alanını temizle
            //ve Notlar sayfasına git

            konuET.setText("");
            icerikET.setText("");

            Intent i = new Intent(NotEkleActivity.this, NotlarActivity.class);
            startActivity(i);
            finish();

            progressDialog.dismiss();
        }

    }

}

