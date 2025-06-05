package com.example.manchingu.adapter; // Sesuaikan package Anda

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.example.manchingu.R; // Sesuaikan package R Anda
import com.example.manchingu.response.ComicResponse; // Import model ComicResponse.Item

import java.util.ArrayList;
import java.util.List;

public class BannerPagerAdapter extends RecyclerView.Adapter<BannerPagerAdapter.BannerViewHolder> {

    // List data asli
    private List<ComicResponse.Item> actualBannerList;
    // Listener untuk klik item
    private OnComicClickListener listener;

    // Interface listener untuk notifikasi klik
    public interface OnComicClickListener {
        void onComicClick(ComicResponse.Item comic);
    }

    // Constructor
    public BannerPagerAdapter(OnComicClickListener listener) {
        // Mulai dengan list kosong
        this.actualBannerList = new ArrayList<>();
        this.listener = listener;
    }

    // Method untuk mengupdate data
    public void updateData(List<ComicResponse.Item> newList) {
        this.actualBannerList = newList;
    }

    // Mendapatkan item data asli berdasarkan posisi ViewPager2
    private ComicResponse.Item getActualItem(int position) {
        if (actualBannerList == null || actualBannerList.isEmpty()) {
            return null;
        }
        // Menggunakan modulo untuk memetakan posisi besar ke index data asli
        int actualPosition = position % actualBannerList.size();
        return actualBannerList.get(actualPosition);
    }

    // --- Implementasi Infinite Loop ---
    @Override
    public int getItemCount() {
        // Jika list data kosong, kembalikan 0
        if (actualBannerList == null || actualBannerList.isEmpty()) {
            return 0;
        }
        // Jika hanya ada 1 item, kembalikan 1 (tidak bisa loop)
        if (actualBannerList.size() == 1) {
            return 1;
        }
        // Jika ada lebih dari 1 item, laporkan jumlah item yang sangat besar
        // untuk mensimulasikan loop tak terbatas
        return Integer.MAX_VALUE;
    }


    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        // Ambil item data asli menggunakan posisi yang dimodulo
        ComicResponse.Item currentComic = getActualItem(position);

        if (currentComic != null) {
            // Load gambar banner menggunakan Glide dari URL poster komik
            Glide.with(holder.itemView.getContext())
                    .load(currentComic.getPoster())

                    .into(holder.bannerImage);

            // Set judul banner
            holder.bannerTitle.setText(currentComic.getName());

            // Set OnClickListener pada itemView
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onComicClick(currentComic);
                }
            });
        } else {
            // Handle kasus item null atau list kosong
            holder.bannerTitle.setText("Error Loading");
            holder.itemView.setOnClickListener(null); // Nonaktifkan klik
        }
    }

    // ViewHolder untuk menyimpan referensi view dari item_banner.xml
    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView bannerTitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerItemImage);
            bannerTitle = itemView.findViewById(R.id.bannerItemTitle);
        }
    }

    // Metode untuk mendapatkan ukuran list data asli
    public int getActualItemCount() {
        return actualBannerList != null ? actualBannerList.size() : 0;
    }
}
