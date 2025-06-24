package com.example.projectblockchain;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private KatalogAdapter katalogAdapter;
    private List<Katalog> katalogList;
    private DatabaseReference databaseReference;
    private Button btnTambah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        recyclerView = findViewById(R.id.rvDashboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        katalogList = new ArrayList<>();
        katalogAdapter = new KatalogAdapter(this, katalogList);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                katalogList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Katalog katalog = snapshot.getValue(Katalog.class);
                    katalogList.add(katalog);
                }
                katalogAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(katalogAdapter);

        btnTambah = findViewById(R.id.btnTambah);
        btnTambah.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.add_catalog, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create(); // supaya bisa dismiss nanti
            dialog.show();

            EditText etNama, etDeskripsi, etNominal, etStok;

            etNama = dialogView.findViewById(R.id.etNama);
            etDeskripsi = dialogView.findViewById(R.id.etDeskripsi);
            etNominal = dialogView.findViewById(R.id.etNominal);
            etStok = dialogView.findViewById(R.id.etStok);
            Button btnTambah = dialogView.findViewById(R.id.btnTambah);

            btnTambah.setOnClickListener(v -> {
                String nama = etNama.getText().toString().trim();
                String deskripsi = etDeskripsi.getText().toString().trim();
                String hargaStr = etNominal.getText().toString().trim();
                String stokStr = etStok.getText().toString().trim();

                if (nama.isEmpty() || deskripsi.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                    Toast.makeText(DashboardActivity.this, "Isi semua field!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int harga = Integer.parseInt(hargaStr);
                int stok = Integer.parseInt(stokStr);

                String id = databaseReference.push().getKey();
                Katalog katalog = new Katalog(id, nama, deskripsi, harga, stok);

                if (id != null ){
                    databaseReference.child(id).setValue(katalog).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menyimpan produk", Toast.LENGTH_SHORT).show();
                    });
                }
                dialog.dismiss();
            });
        });
    }
}
