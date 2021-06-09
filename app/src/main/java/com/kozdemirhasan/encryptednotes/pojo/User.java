package com.kozdemirhasan.encryptednotes.pojo;

/**
 * Created by Casper on 17.02.2018.
 */

public class User {
    int userId;
    String password;
    int silmeDurum;
    int silmeGun;
    int metinBoyutu;

    public User() {
    }

    public User(int userId, String password) {
        this.userId = userId;
        this.password = password;

    }

    public User(int userId, String password,  int silmeDurum, int silmeGun, int metinBoyutu) {
        this.userId = userId;
        this.password = password;
              this.silmeDurum = silmeDurum;
        this.silmeGun = silmeGun;
        this.metinBoyutu = metinBoyutu;
    }

    public User(int silmeDurum, int silmeGun, int metinBoyutu) {

        this.silmeDurum = silmeDurum;
        this.silmeGun = silmeGun;
        this.metinBoyutu = metinBoyutu;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSilmeDurum() {
        return silmeDurum;
    }

    public void setSilmeDurum(int silmeDurum) {
        this.silmeDurum = silmeDurum;
    }

    public int getSilmeGun() {
        return silmeGun;
    }

    public void setSilmeGun(int silmeGun) {
        this.silmeGun = silmeGun;
    }

    public int getMetinBoyutu() {
        return metinBoyutu;
    }

    public void setMetinBoyutu(int metinBoyutu) {
        this.metinBoyutu = metinBoyutu;
    }
}
