package com.kozdemirhasan.encryptednotes.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.kozdemirhasan.encryptednotes.database.UserDatabase;
import com.kozdemirhasan.encryptednotes.R;
import com.kozdemirhasan.encryptednotes.pojo.User;

public class AyarlarActivity extends AppCompatActivity {
    Switch switchEskiNotlarSil;
    String eskiTarihText = "Delete old notes";
    private static User ayarx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ayarlar);
        switchEskiNotlarSil = (Switch) findViewById(R.id.switchEskiNotlarSil);

        //ayarx değerlerini getir ve yaz
        ayarBilgileriGetir();
        ayarlarıYaz();


        switchEskiNotlarSil.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    silinecekTarihBilgisi();
                    silmeDurumTrueYap();
                } else {
                    switchEskiNotlarSil.setText(eskiTarihText);
                    silmeDurumFalseYap();
                }
            }
        });

    }


    private void ayarBilgileriGetir() {
        UserDatabase dba = new UserDatabase(AyarlarActivity.this);
        dba.ac();
        ayarx = dba.ayarlar();
        dba.kapat();
    }

    private void ayarlarıYaz() {
        if (ayarx.getSilmeDurum() == 1) {
            switchEskiNotlarSil.setChecked(true);
            switchEskiNotlarSil.setText(eskiTarihText + "\n(" + ayarx.getSilmeGun() + " gün ve eski)");

        } else if (ayarx.getSilmeDurum() == 0) {
            switchEskiNotlarSil.setChecked(false);
            switchEskiNotlarSil.setText(eskiTarihText);
        }
    }

    public void silinecekTarihBilgisi() {
        ayarBilgileriGetir();
        AlertDialog.Builder builder = new AlertDialog.Builder(AyarlarActivity.this);
        builder.setTitle("Enter the number of days");

        final EditText input = new EditText(AyarlarActivity.this);

        input.setText(String.valueOf(ayarx.getSilmeGun()));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setMessage("WARNING...\nThe selected date and pre-created messages are deleted.");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             try{
                 UserDatabase dba = new UserDatabase(AyarlarActivity.this);
                 dba.ac();
                 int drm = dba.gunSayisiSetEt(Integer.parseInt(input.getText().toString()));
                 dba.kapat();

                 if (drm != -1) {
                     silmeDurumTrueYap();
                     switchEskiNotlarSil.setText(eskiTarihText + "\n(" + String.valueOf(ayarx.getSilmeGun()) + " days and old)");


                 } else {
                     silmeDurumFalseYap();
                 }

             }catch (Exception ex){
                 Toast.makeText(AyarlarActivity.this,"An error occurred !!!\n(Please enter the number of days)",Toast.LENGTH_LONG).show();
                 silmeDurumFalseYap();
             }



            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                silmeDurumFalseYap();
                switchEskiNotlarSil.setText(eskiTarihText);
                dialog.cancel();

            }
        });
        // switchEskiNotlarSil.setText(eskiTarihText + "\n(" + String.valueOf(ayarx.get("GUNSAYISI")) + " gün ve eski)");
        builder.show();
        ayarBilgileriGetir();
    }


    public void silmeDurumTrueYap() {
        UserDatabase dba = new UserDatabase(this);
        dba.ac();
        int drm = dba.silmeDurumTrueYap();
        dba.kapat();

        switchEskiNotlarSil.setChecked(true);

        ayarBilgileriGetir();

    }

    public void silmeDurumFalseYap() {
        UserDatabase dba = new UserDatabase(this);
        dba.ac();
        int drm = dba.silmeDurumFalseYap();
        dba.kapat();

        switchEskiNotlarSil.setChecked(false);

        ayarBilgileriGetir();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Eğer geri butonuna basılırsa
            //önce silme durumu aktif ise tarihi geçen notları isl sonra notlar sayfasına git ???

            Intent i = new Intent(AyarlarActivity.this,
                    NotlarActivity.class);
            startActivity(i);
            finish();


        }
        return super.onKeyDown(keyCode, event);
    }
}
