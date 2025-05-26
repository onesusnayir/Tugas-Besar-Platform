package com.example.manchingu.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

public class HomeActivity extends AppCompatActivity {
    SharedPreferences prefs;
    String username;
    TextView tvUsername;

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
        username = prefs.getString("username", "defaultName");
        tvUsername = findViewById(R.id.username);
        tvUsername.setText(username);

        rvRekomendasi = findViewById(R.id.rvRekomendasi);
        rvRekomendasi.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AllComicAdapter(this, comicList, comic -> {
            // TODO: Intent ke detail activity jika ingin
        });
        rvRekomendasi.setAdapter(adapter);

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
    }
}
