package com.kozdemirhasan.encryptednotes.pojo;

public class Note {
    public int _id;
    public String konu;
    public String icerik;
    public String kayittarihi;
    public String grup;
    public long trh;


    public Note() {
    }

    public Note(int _id, long trh) {
        this._id = _id;
        this.trh = trh;
    }



    public Note(int _id, String konu, String icerik, String kayittarihi) {
        this._id = _id;
        this.konu = konu;
        this.icerik = icerik;
        this.kayittarihi = kayittarihi;

    }

    public Note(int _id, String konu, String icerik, String kayittarihi, String grup) {
        this._id = _id;
        this.konu = konu;
        this.icerik = icerik;
        this.kayittarihi = kayittarihi;
        this.grup = grup;
    }

    public Note(int _id, String grup, String konu, String icerik, long trh) {
        this._id = _id;
        this.konu = konu;
        this.icerik = icerik;
        this.grup = grup;
        this.trh = trh;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getKonu() {
        return konu;
    }

    public void setKonu(String konu) {
        this.konu = konu;
    }

    public String getIcerik() {
        return icerik;
    }

    public void setIcerik(String icerik) {
        this.icerik = icerik;
    }

    public String getKayittarihi() {
        return kayittarihi;
    }

    public void setKayittarihi(String kayittarihi) {
        this.kayittarihi = kayittarihi;
    }

    public String getGrup() {
        return grup;
    }

    public void setGrup(String grup) {
        this.grup = grup;
    }

    public long getTrh() {
        return trh;
    }

    public void setTrh(long trh) {
        this.trh = trh;
    }
}