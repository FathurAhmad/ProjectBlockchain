package com.example.projectblockchain;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class KatalogAdapter extends RecyclerView.Adapter<KatalogAdapter.KatalogViewHolder> {
    private List<Katalog> katalogList;
    private Context context;
    public KatalogAdapter(Context context, List<Katalog> katalogList) {
        this.context = context;
        this.katalogList = katalogList;
    }

    public class KatalogViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDeskripsi, tvHarga, tvStok;
        public KatalogViewHolder(View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvStok = itemView.findViewById(R.id.tvStok);
        }
    }

    @Override
    public KatalogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_catalog, parent, false);
        return new KatalogViewHolder(view);
    }
    @Override
    public void onBindViewHolder(KatalogViewHolder holder, int position) {
        Katalog katalog = katalogList.get(position);
        holder.tvNama.setText(katalog.getNama());
        holder.tvDeskripsi.setText(katalog.getDeskripsi());
        holder.tvHarga.setText(String.valueOf(katalog.getHarga()));
        holder.tvStok.setText(String.valueOf(katalog.getStok()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, KatalogDetailActivity.class);
            intent.putExtra("nama", katalog.getNama());
            intent.putExtra("deskripsi", katalog.getDeskripsi());
            intent.putExtra("harga", katalog.getHarga());
            intent.putExtra("stok", katalog.getStok());
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return katalogList.size();
    }
}
