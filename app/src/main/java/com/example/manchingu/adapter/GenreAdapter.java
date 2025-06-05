package com.example.manchingu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
    private final List<String> genreList;
    public GenreAdapter(List<String> genreList) {
        this.genreList = genreList;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        String genre = genreList.get(position);
        holder.genreText.setText(genre);
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView genreText;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            genreText = itemView.findViewById(R.id.ivGenre);
        }
    }
}
