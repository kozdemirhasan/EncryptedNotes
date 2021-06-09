package com.kozdemirhasan.encryptednotes.util;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kozdemirhasan.encryptednotes.database.NotesDatabase;
import com.kozdemirhasan.encryptednotes.database.UserDatabase;
import com.kozdemirhasan.encryptednotes.R;
import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.User;
import com.kozdemirhasan.encryptednotes.pojo.Note;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Button btnGiris;
    EditText etParola;
    // HashMap<String, Integer> ayar;
    User ayar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.main);
        setTitle("Encrypted Notes");


        etParola = (EditText) findViewById(R.id.etParola);
        btnGiris = (Button) findViewById(R.id.btGiris);

        kullaniciKnt();//kayıtlı kullanıcı olup olmadığını kontrol et, yoksa yeni kullanıcı kayıt yap

        ayarBilgileriGetir();

        Sabitler.yaziBoyutu = ayar.getMetinBoyutu();

        if (ayar.getSilmeDurum() == 1) {
            tarihiGecenNotlariSil();
        }

        btnGiris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giris();
            }
        });

    }


    public void ayarBilgileriGetir() {
        //kullanıcı bilgilerini Sabitler sayfasındaki değerlere yaz
        UserDatabase dba = new UserDatabase(MainActivity.this);
        dba.ac();
        ayar = dba.ayarlar();
        Sabitler.PASS_MD5 = ayar.getPassword();
        Sabitler.yaziBoyutu = ayar.getMetinBoyutu();
        dba.kapat();
    }

    public void giris() {
        boolean passDrm = passwordKonrol(etParola.getText().toString());


        if (passDrm == true) {
            //parolayı Sabitler sayfasında yaz
            Sabitler.loginPassword = etParola.getText().toString();

            Intent m = new Intent(MainActivity.this,
                    NotlarActivity.class);
            startActivity(m);
            finish();

        } else {
            Toast.makeText(getApplicationContext(),
                    "Incorrect password", Toast.LENGTH_SHORT).show();
        }
    }

    private void tarihiGecenNotlariSil() {
//Veritabanından tüm notları al getir
        NotesDatabase dba = new NotesDatabase(MainActivity.this);
        dba.ac();
        List<Note> notlar = dba.tumKayitlar();
        //notları tarihlerine göre kontrol et ve silinecek olanları sil
        if (notlar.size() > 0) {
            List<Integer> silinecekler = new ArrayList<Integer>();
            long nowTime = System.currentTimeMillis();
            long gs = ayar.getSilmeGun();
            long x = (gs * 24 * 60 * 60 * 1000);

            for (int i = 0; i < notlar.size(); i++) {
                //notun tarihini kontrol et tarihi geçmişse silinceklere ekle
                //   Date nowTime = new Date();

                if (notlar.get(i).getTrh() + x < nowTime) {
                    silinecekler.add(notlar.get(i).get_id());
                }
            }
            dba.eskileriSil(silinecekler);
        }
        dba.kapat();
    }

    private void kullaniciKnt() {
        UserDatabase db = new UserDatabase(MainActivity.this);
        db.ac();
        boolean drm = db.kullaniciVarmiKontrolEt();
        db.kapat();

        if (!drm) {
            //kullanÄ±cÄ± kayÄ±t sayfasÄ±na git
            Intent i = new Intent(this,
                    KullaniciKayitActivity.class);
            startActivity(i);
            finish();
        }
    }


    private boolean passwordKonrol(String password) {
        UserDatabase db = new UserDatabase(MainActivity.this);
        db.ac();

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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Eğer geri butonuna basılırsa

            finish();
            System.exit(0);

            //  android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
