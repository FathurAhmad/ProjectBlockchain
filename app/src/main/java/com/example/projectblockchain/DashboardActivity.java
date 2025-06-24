package com.example.projectblockchain;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private Button btnBeli;
    private TextView tvNama, tvDeskripsi, tvHarga, tvStok;

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

    }
}
