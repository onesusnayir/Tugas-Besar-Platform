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
import com.example.manchingu.R;
import com.example.manchingu.response.BookmarkResponse;

import java.util.List;

public class BookmarkAdapter
        extends RecyclerView.Adapter<BookmarkAdapter.ComicViewHolder> {
    private Context context;
    private List<BookmarkResponse.Comic> comicList;
    public interface OnComicClickListener {
        void onComicClick(BookmarkResponse.Comic comic);
    }
    private BookmarkAdapter.OnComicClickListener listener;

    public BookmarkAdapter(Context context, List<BookmarkResponse.Comic> comicList, BookmarkAdapter.OnComicClickListener listener){
        this.context = context;
        this.comicList = comicList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public BookmarkAdapter.ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comic_grid, parent, false);
        return new BookmarkAdapter.ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.ComicViewHolder holder, int position) {
        BookmarkResponse.Comic comic = comicList.get(position);

        holder.tvName.setText(comic.getName());
        holder.tvAuthor.setText(comic.getAuthor());

        Glide.with(context)
                .load(comic.getPoster())
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComicClick(comic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comicList != null ? comicList.size() : 0;
    }

    public class ComicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvName, tvAuthor;
        public ComicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvName = itemView.findViewById(R.id.tvName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}
