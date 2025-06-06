package com.example.manchingu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;
import com.example.manchingu.adapter.AllComicAdapter;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.fragments.HomeFragment;
import com.example.manchingu.response.ComicResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllComicsActivity extends AppCompatActivity implements View.OnClickListener {
    // RecyclerView Comics
    RecyclerView rvComics;
    AllComicAdapter adapter;

    // List Comics
    List<ComicResponse.Item> comicList = new ArrayList<>();

    // Button
    ImageView backBtn;

    // Loading
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_comics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inisialisasi
        rvComics = findViewById(R.id.rv_comics);
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        adapter = new AllComicAdapter(this, comicList, comic -> {
            // Comic onClick: Pindah Ke Halaman Comic Detail Activity
            Intent intent = new Intent(AllComicsActivity.this, ComicDetailActivity.class);
            intent.putExtra("title", comic.getName());
            intent.putExtra("author", comic.getAuthor());
            intent.putExtra("poster", comic.getPoster());
            intent.putExtra("synopsis", comic.getSynopsis());
            intent.putExtra("id_comic", comic.getId_comic());
            intent.putExtra("artist", comic.getArtist());
            startActivity(intent);
        });
        rvComics.setAdapter(adapter);

        // Ambil ApiService dari ApiClient
        ApiService apiService = ApiClient.getApiService(this);

        // Panggil API
        apiService.getAllComics().enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(Call<ComicResponse> call, Response<ComicResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    progressBar.setVisibility(View.GONE);
                    comicList.clear();
                    comicList.addAll(response.body().getData().getItems());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ComicResponse> call, Throwable t) {
                // Handle error, misal tampilkan Toast
            }
        });

        backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}