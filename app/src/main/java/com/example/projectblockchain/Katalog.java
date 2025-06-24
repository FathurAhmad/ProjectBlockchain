package com.example.projectblockchain;

public class Katalog {
    String id;
    String nama;
    String deskripsi;
    int harga;
    int stok;
    public Katalog () {

    }
    public Katalog(String id, String nama, String deskripsi, int harga, int stok) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNama() {
        return nama;
        }
    public void setNama(String nama) {
        this.nama = nama;
    }
    public String getDeskripsi() {
        return deskripsi;
    }
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
        }
    public int getHarga() {
        return harga;
    }
    public void setHarga(int harga) {
        this.harga = harga;
    }
    public int getStok() {
        return stok;
    }
    public void setStok(int stok) {
        this.stok = stok;
    }
}
