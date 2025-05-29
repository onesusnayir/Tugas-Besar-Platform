package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.manchingu.R;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.BookmarkResponse;
import com.example.manchingu.response.UserResponse;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private Button bookmarkBtn;
    private ApiService apiService;
    private String token;
    private SharedPreferences prefs;
    private List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    private boolean isExsist = false;
    private String BookmarkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comic_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ambil data dari Intent
        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String description = getIntent().getStringExtra("synopsis");
        String posterUrl = getIntent().getStringExtra("poster");

        // Temukan view-nya
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvDescription = findViewById(R.id.tvSynopsis);
        ImageView ivPoster = findViewById(R.id.ivPoster);

        // Set data ke view
        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvDescription.setText(description);
        Window window = getWindow();
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_blue)); // samakan dengan warna BottomNavigationView
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue)); // jika ingin atas juga sama

        // Tampilkan gambar dengan Glide
        Glide.with(this)
                .load(posterUrl)
                .into(ivPoster);

        // Get Token dari SharedPrefereces
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token","");

        apiService = ApiClient.getApiService(this);

        bookmarkBtn = findViewById(R.id.bookmark_btn);
        bookmarkBtn.setOnClickListener(this);

        getBookmarkList();
    }

    @Override
    public void onClick(View v) {
        String idComic = getIntent().getStringExtra("id_comic");

        if(isExsist){
            deleteBookmark();
            bookmarkBtn.setText("Add Bookmark");
            isExsist = !isExsist;
        }else{
            insertBookmark(idComic);
            bookmarkBtn.setText("Delete Bookmark");
            isExsist = !isExsist;
        }
    }

    private void getBookmarkList() {
        apiService.getAllMyBookmark("Bearer "+token)
            .enqueue(new Callback<BookmarkResponse>() {
                 @Override
                 public void onResponse(Call<BookmarkResponse> call, Response<BookmarkResponse> response) {
                     if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                         comicList.clear();
                         String idComic = getIntent().getStringExtra("id_comic");
                         for (BookmarkResponse.Data bookmark : response.body().getData()) {
                             BookmarkResponse.Comic comic = bookmark.getComic();
                             comicList.add(comic);

                             // cek apakah idComic cocok
                             if (comic.getId_comic().equals(idComic)) {
                                 isExsist = true;
                                 bookmarkBtn.setText("Delete Bookmark");
                                 BookmarkId = bookmark.getId_bookmark();
                                 break;
                             }
                         }
                     }
                 }

                 @Override
                 public void onFailure(Call<BookmarkResponse> call, Throwable t) {

                 }
            }
        );
    }


    private void insertBookmark(String idComic) {
        apiService.insertNewBookmark("Bearer "+token, idComic,"COMPLETED")
                .enqueue(new Callback<JsonObject>(){
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String message = response.body().get("message").getAsString();

                            Toast.makeText(ComicDetailActivity.this, message, Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                    }
                });
    }

    private void deleteBookmark() {
        apiService.deleteBookmark("Bearer "+token, BookmarkId)
                .enqueue(new Callback<JsonObject>(){
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject res = response.body();

                            Toast.makeText(ComicDetailActivity.this, res.get("message").getAsString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                    }
                });

    }


}