package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    SharedPreferences prefs;
    TextView tvUsername;
    Button seeComicsBtn;
    BottomNavigationView bottomNav;
    RecyclerView rvRekomendasi;
    AllComicAdapter adapter;
    List<ComicResponse.Item> comicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "defaultName");
        tvUsername = findViewById(R.id.username);
        tvUsername.setText(username);

        rvRekomendasi = findViewById(R.id.rvRekomendasi);
        rvRekomendasi.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AllComicAdapter(this, comicList, comic -> {
            // TODO: Intent ke detail activity jika ingin
            Intent intent = new Intent(HomeActivity.this, ComicDetailActivity.class);
            intent.putExtra("title", comic.getName());
            intent.putExtra("author", comic.getAuthor());
            intent.putExtra("artist", comic.getArtist());
            intent.putExtra("poster", comic.getPoster()); // URL atau drawable name
            intent.putExtra("synopsis", comic.getSynopsis()); // jika ada
            intent.putExtra("id_comic", comic.getId_comic());
            intent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
            startActivity(intent);
        });
        rvRekomendasi.setAdapter(adapter);

        // Ambil ApiService dari ApiClient
        ApiService apiService = ApiClient.getApiService(this);

        // Panggil API
        apiService.getLimitedComics(1, 10).enqueue(new Callback<ComicResponse>() {
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

        seeComicsBtn = findViewById(R.id.see_all_btn);
        seeComicsBtn.setOnClickListener(this);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.see_all_btn){
            Intent intent = new Intent(HomeActivity.this, AllComicsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_bookmark){
            Intent intent = new Intent(HomeActivity.this, BookmarkActivity.class);
            startActivity(intent);
        }
        return false;
    }
}
