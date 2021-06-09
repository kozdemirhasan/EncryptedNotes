package com.kozdemirhasan.encryptednotes.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kozdemirhasan.encryptednotes.R;
import com.kozdemirhasan.encryptednotes.pojo.Crypt;
import com.kozdemirhasan.encryptednotes.pojo.Note;
import com.kozdemirhasan.encryptednotes.pojo.Sabitler;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> basliklar;
    private HashMap<String, ArrayList<Note>> icerik;

    public ExpListAdapter(Context context, ArrayList<String> basliklar, HashMap<String, ArrayList<Note>> icerik) {
        super();
        this.context = context;
        this.basliklar = basliklar;
        this.icerik = icerik;

    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        return icerik.get(basliklar.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {

        final Note note = (Note) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.notlar_layout, null);
        }

        TextView baslik = (TextView) convertView.findViewById(R.id.txtBaslik);
        Crypt crypt = new Crypt();
        try {
            baslik.setText(crypt.decrypt(note.getKonu(), Sabitler.loginPassword));
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tarih = (TextView) convertView.findViewById(R.id.txtTarih);
        tarih.setText(note.getKayittarihi());

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.chkSecim);
        checkBox.setChecked(false);
        checkBox.setVisibility(View.INVISIBLE);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return icerik.get(basliklar.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
          return basliklar.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
          return basliklar.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
           return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String baslik = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.liste_grup, null);
        }
        Crypt crypt = new Crypt();
        TextView grup = (TextView) convertView.findViewById(R.id.txtGrup);
        try {
            grup.setText(crypt.decrypt(baslik,Sabitler.loginPassword));
        } catch (Exception e) {
            e.printStackTrace();
        }


        return convertView;
    }

    @Override
    public boolean hasStableIds() {

        return false;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {

        return true;
    }

}
