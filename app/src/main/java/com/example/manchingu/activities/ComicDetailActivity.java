package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manchingu.R;
import com.example.manchingu.adapter.GenreAdapter;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.fragments.HomeFragment;
import com.example.manchingu.response.BookmarkResponse;
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
    ImageView backBtn;

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
        String artist = getIntent().getStringExtra("artist");
        String description = getIntent().getStringExtra("synopsis");
        String posterUrl = getIntent().getStringExtra("poster");
//        ArrayList genre = getIntent().getIntegerArrayListExtra("genre");

        // Genre
        ArrayList<String> genre = getIntent().getStringArrayListExtra("genre");
        RecyclerView rvGenre = findViewById(R.id.rvGenre);
        rvGenre.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (genre != null) {
            GenreAdapter genreAdapter = new GenreAdapter(genre);
            rvGenre.setAdapter(genreAdapter);
        }

        // Temukan view-nya
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvDescription = findViewById(R.id.tvSynopsis);
        ImageView ivPoster = findViewById(R.id.ivPoster);
        TextView tvArtist = findViewById(R.id.tvArtist);

        // Set data ke view
        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvDescription.setText(description);
        tvArtist.setText(artist);

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

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(this);

        getBookmarkList();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bookmark_btn) {
            showBookmarkDialog();
        } else if (v.getId() == R.id.back_btn) {
            finish();
        }
    }

    private void showBookmarkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.bookmark_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(
                ContextCompat.getDrawable(this, R.color.background)
        );

        AutoCompleteTextView dropdown = dialogView.findViewById(R.id.bookmark_dropdown);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Dropdown status
        String[] statusOptions = {"READING", "COMPLETED", "PLAN_TO_READ", "DROPPED", "NONE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );
        dropdown.setAdapter(adapter);

        // Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Save bookmark
        btnSave.setOnClickListener(v -> {
            String selectedStatus = dropdown.getText().toString().trim();
            String idComic = getIntent().getStringExtra("id_comic");

            if (selectedStatus.isEmpty()) {
                Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedStatus.equalsIgnoreCase("None")) {
                if (isExsist) {
                    deleteBookmark();
                    bookmarkBtn.setText("Add Bookmark");
                    isExsist = false;
                } else {
                    Toast.makeText(this, "Bookmark belum ada", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isExsist) {
                    updateBookmark(BookmarkId, selectedStatus);
                } else {
                    insertBookmark(idComic, selectedStatus);
                }
                bookmarkBtn.setText(selectedStatus);
                isExsist = true;
            }

//            if (selectedStatus.equalsIgnoreCase("Completed")){
//                Log.d("Test", selectedStatus);
//            }

            dialog.dismiss();
        });


        dialog.show();
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
                                 String status = bookmark.getStatus();
                                 bookmarkBtn.setText(status);
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


    private void insertBookmark(String idComic, String selectedStatus) {
        apiService.insertNewBookmark("Bearer "+token, idComic,selectedStatus)
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

    private void updateBookmark(String bookmarkId, String selectedStatus) {
        apiService.updateBookmark("Bearer " + token, BookmarkId, selectedStatus)
                .enqueue(new Callback<JsonObject>() {
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