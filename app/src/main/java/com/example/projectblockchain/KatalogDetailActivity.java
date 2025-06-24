package com.example.projectblockchain;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class KatalogDetailActivity extends AppCompatActivity {
    private TextView tvNama, tvDeskripsi, tvHarga, tvStok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.item_detail);
        tvNama = findViewById(R.id.tvNama);
        tvDeskripsi = findViewById(R.id.tvDeskripsi);
        tvHarga = findViewById(R.id.tvHarga);
        tvStok = findViewById(R.id.tvStok);

        String nama = getIntent().getStringExtra("nama");
        String deskripsi = getIntent().getStringExtra("deskripsi");
        int harga = getIntent().getIntExtra("harga", 0);
        int stok = getIntent().getIntExtra("stok", 0);

        tvNama.setText(nama);
        tvDeskripsi.setText(deskripsi);
        tvHarga.setText(String.valueOf(harga));
        tvStok.setText(String.valueOf(stok));
    }
}
