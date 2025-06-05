package com.example.manchingu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manchingu.R; // Pastikan ini di-import
import com.example.manchingu.response.ComicResponse; // Import kelas ComicResponse Anda

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context context;
    // Menggunakan Item dari ComicResponse
    private List<ComicResponse.Item> comicList;
    private OnComicClickListener listener;

    // Interface untuk klik item
    public interface OnComicClickListener {
        void onComicClick(ComicResponse.Item comic);
    }

    // Constructor
    public SearchAdapter(Context context, List<ComicResponse.Item> comicList, OnComicClickListener listener) {
        this.context = context;
        this.comicList = comicList;
        this.listener = listener;
    }

    // Method untuk memperbarui data setelah search
    public void updateData(List<ComicResponse.Item> newComicList) {
        this.comicList = newComicList;
        notifyDataSetChanged(); // Memberi tahu RecyclerView data berubah
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comic_grid, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        // Mengambil objek ComicResponse.Item pada posisi saat ini
        ComicResponse.Item comic = comicList.get(position);

        holder.tvName.setText(comic.getName());
        holder.tvAuthor.setText(comic.getAuthor());

        // Menggunakan Glide untuk memuat gambar poster
        Glide.with(context)
                .load(comic.getPoster())
                .into(holder.ivPoster);

        // Menangani klik pada item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComicClick(comic);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Mengembalikan jumlah total item
        return comicList != null ? comicList.size() : 0;
    }

    // Inner class ViewHolder
    // Menampung referensi ke View untuk satu item
    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        // Deklarasikan View yang ada di layout R.layout.item_comic_grid
        ImageView ivPoster;
        TextView tvName, tvAuthor;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            // Menghubungkan variabel dengan View di layout menggunakan ID
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvName = itemView.findViewById(R.id.tvName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}
