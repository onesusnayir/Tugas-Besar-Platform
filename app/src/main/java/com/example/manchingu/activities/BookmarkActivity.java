package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private RecyclerView rvBookmark;
    private SharedPreferences prefs;
    private List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    private BookmarkAdapter adapter;
    private ApiService apiService;
    private String token;
    private TextView CompletedBtn, ReadingBtn, DroppedBtn, PlanToReadBtn;
    private BottomNavigationView bottomNav;

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

        // Inisialisasi
        rvBookmark = findViewById(R.id.rvBookmark);
        CompletedBtn = findViewById(R.id.completed_btn);
        ReadingBtn = findViewById(R.id.reading_btn);
        DroppedBtn = findViewById(R.id.dropped_btn);
        PlanToReadBtn = findViewById(R.id.plan_to_read_btn);

        ShowBookmarkRecyclerView();

        // Get Token dari SharedPrefereces
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token","");
        Log.d("TAG", token);

        // Memanggil fungsi ApiClient.getApiService()
        apiService = ApiClient.getApiService(this);

        GetBookmark("COMPLETED");
        CompletedBtn.setBackgroundResource(R.drawable.rounded);
        CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.backgroundBtn));

        // SetOnClickListener
        CompletedBtn.setOnClickListener(this);
        ReadingBtn.setOnClickListener(this);
        DroppedBtn.setOnClickListener(this);
        PlanToReadBtn.setOnClickListener(this);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_bookmark);
        bottomNav.setOnNavigationItemSelectedListener(this);
    }

    //    Metohod Recycler view
    private void ShowBookmarkRecyclerView() {
        rvBookmark.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookmarkAdapter(this, comicList, new BookmarkAdapter.OnComicClickListener() {
            @Override
            public void onComicClick(BookmarkResponse.Comic comic) {
                Intent intent = new Intent(BookmarkActivity.this, ComicDetailActivity.class);
                intent.putExtra("title", comic.getName());
                intent.putExtra("author", comic.getAuthor());
                intent.putExtra("poster", comic.getPoster());
                intent.putExtra("synopsis", comic.getSynopsis());
                intent.putExtra("id_comic", comic.getId_comic());
                startActivity(intent);
            }
        });
        rvBookmark.setAdapter(adapter);
    }

    private void GetBookmark(String status) {
        // memanggil method ApiService.getAllMyBookmark dengan token user
        apiService.getMyBookmark("Bearer "+token, status)
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
            }
        );
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.completed_btn){
            GetBookmark("COMPLETED");
            CompletedBtn.setBackgroundResource(R.drawable.rounded);
            CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.backgroundBtn));

            DroppedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        else if (v.getId() == R.id.dropped_btn) {
            GetBookmark("DROPPED");
            DroppedBtn.setBackgroundResource(R.drawable.rounded);
            DroppedBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        else if (v.getId() == R.id.reading_btn) {
            GetBookmark("READING");
            ReadingBtn.setBackgroundResource(R.drawable.rounded);
            ReadingBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            DroppedBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        else if (v.getId() == R.id.plan_to_read_btn) {
            GetBookmark("PLAN_TO_READ");
            PlanToReadBtn.setBackgroundResource(R.drawable.rounded);
            PlanToReadBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            DroppedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home){
            Intent intent = new Intent(BookmarkActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.nav_search){
            Intent intent = new Intent(BookmarkActivity.this, SearchActivity.class);
            startActivity(intent);
        }
        return false;
    }
}