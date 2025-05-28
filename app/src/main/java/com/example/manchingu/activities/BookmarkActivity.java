package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;
import com.example.manchingu.adapter.AllComicAdapter;
import com.example.manchingu.adapter.BookmarkAdapter;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.BookmarkResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkActivity extends AppCompatActivity {
    RecyclerView rvBookmark;
    SharedPreferences prefs;
    List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    BookmarkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookmark);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvBookmark = findViewById(R.id.rvBookmark);
        rvBookmark.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookmarkAdapter(this, comicList, new BookmarkAdapter.OnComicClickListener() {
            @Override
            public void onComicClick(BookmarkResponse.Comic comic) {
                Intent intent = new Intent(BookmarkActivity.this, ComicDetailActivity.class);
                intent.putExtra("title", comic.getName());
                intent.putExtra("author", comic.getAuthor());
                intent.putExtra("poster", comic.getPoster());
                intent.putExtra("synopsis", comic.getSynopsis());
                startActivity(intent);
            }
        });
        rvBookmark.setAdapter(adapter);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token","");
        Log.d("TAG", token);

        // Ambil ApiService dari ApiClient
        ApiService apiService = ApiClient.getApiService(this);

        apiService.getAllMyBookmark("Bearer "+token)
                .enqueue(new Callback<BookmarkResponse>() {
                    @Override
                    public void onResponse(Call<BookmarkResponse> call, Response<BookmarkResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            comicList.clear();
                            for (BookmarkResponse.Data bookmark : response.body().getData()) {
                                comicList.add(bookmark.getComic());
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<BookmarkResponse> call, Throwable t) {

                    }
                });
    }
}