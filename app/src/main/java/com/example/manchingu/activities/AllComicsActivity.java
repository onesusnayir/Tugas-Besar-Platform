package com.example.manchingu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;
import com.example.manchingu.adapter.AllComicAdapter;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllComicsActivity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView rvComics;
    AllComicAdapter adapter;
    List<ComicResponse.Item> comicList = new ArrayList<>();
    ImageView backBtn;

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
        rvComics = findViewById(R.id.rv_comics);
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new AllComicAdapter(this, comicList, comic -> {
            // TODO: Intent ke detail activity jika ingin
            Intent intent = new Intent(AllComicsActivity.this, ComicDetailActivity.class);
            intent.putExtra("title", comic.getName());
            intent.putExtra("author", comic.getAuthor());
            intent.putExtra("poster", comic.getPoster()); // URL atau drawable name
            intent.putExtra("synopsis", comic.getSynopsis()); // jika ada
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
        Intent intent = new Intent(AllComicsActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}