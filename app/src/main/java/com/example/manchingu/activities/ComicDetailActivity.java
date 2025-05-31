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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.example.manchingu.adapter.ReviewAdapter;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.fragments.HomeFragment;
import com.example.manchingu.response.BookmarkResponse;
import com.example.manchingu.response.ComicResponse;
import com.example.manchingu.response.ProfileResponse;
import com.example.manchingu.response.ReviewResponse;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComicDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private Button bookmarkBtn;
    private ApiService apiService;
    private String token;
    private String idComic;
    private String title;
    private SharedPreferences prefs;
    private List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    private List<ReviewResponse.ReviewData> reviewList = new ArrayList<>();
    private boolean isExsist = false;
    private String BookmarkId;
    private ImageView backBtn;
    private EditText etUlasan;
    private RatingBar ratingBar;
    private ReviewAdapter adapter;
    private RecyclerView rvReview;
    private TextView tvAvgRating;
    private TextView tvComment;
    private ImageView tvColorStatus;
    private LinearLayout postUserReview, getUserReview;
    private Button deleteReviewBtn, editReviewBtn;
    private String reviewId;
    private ReviewResponse.ReviewData reviewData;
    private TextView updateBtn, tvPosting;
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
        title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String artist = getIntent().getStringExtra("artist");
        String description = getIntent().getStringExtra("synopsis");
        String posterUrl = getIntent().getStringExtra("poster");
        String status = getIntent().getStringExtra("status");
        Integer bookmarked = getIntent().getIntExtra("bookmarked", 0);
        idComic = getIntent().getStringExtra("id_comic");
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
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvBookmarkNum = findViewById(R.id.tvBookmarkNum);
        tvColorStatus = findViewById(R.id.tvColorStatus);
        postUserReview = findViewById(R.id.post_user_review);
        getUserReview = findViewById(R.id.get_user_review);
        
        // Inisialisasi Edit Text & Rating Bar
        etUlasan = findViewById(R.id.et_ulasan);
        ratingBar = findViewById(R.id.ratingBar);

        // Set data ke view
        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvDescription.setText(description);
        tvArtist.setText(artist);
        tvStatus.setText(status);
        tvBookmarkNum.setText(Integer.toString(bookmarked));
        if (status != null) {
            if (status.equalsIgnoreCase("ON_GOING")) {
                tvStatus.setText("On-Going");
                tvColorStatus.setColorFilter(ContextCompat.getColor(this, R.color.green));
            } else if (status.equalsIgnoreCase("COMPLETED")) {
                tvStatus.setText("Completed");
                tvColorStatus.setColorFilter(ContextCompat.getColor(this, R.color.light_blue));
            } else {
                tvStatus.setText(status);
            }
        } else {
            tvStatus.setText("Unknown Status");
            tvColorStatus.setVisibility(View.GONE);
        }
        // Adapter review
        adapter = new ReviewAdapter(reviewList,
                this);
        rvReview = findViewById(R.id.rvReview);
        rvReview.setLayoutManager(new LinearLayoutManager(this));
        rvReview.setAdapter(adapter);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvComment = findViewById(R.id.tvComment);


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
        tvPosting = findViewById(R.id.tv_posting);
        tvPosting.setOnClickListener(this);

        // --- Reviews ---
        // Edit & Delete Review
        deleteReviewBtn = findViewById(R.id.delete_user_review);
        deleteReviewBtn.setOnClickListener(this);

        editReviewBtn = findViewById(R.id.edit_user_review);
        editReviewBtn.setOnClickListener(this);

        // Update Review Btn
        updateBtn = findViewById(R.id.tv_update);

        // Method untuk mendapatkan list bookmark: digunakan untuk mengetahui apakah
        // ngebookmark comic ini
        getBookmarkList();

        // Method untuk mendapatkan list review comic ini
        getReviewComics();

        // Method untuk mengetahui apakah user mereview comic ini atau tidak
        getUserReview();
    }

    private void getUserReview() {
        apiService.getUserReview("Bearer "+token, title)
            .enqueue(new Callback<ReviewResponse>() {
                    @Override
                    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body() != null) {
                            List<ReviewResponse.ReviewData> items = response.body().getData();
                            if (items.isEmpty()){
                                postUserReview.setVisibility(View.VISIBLE);
                                getUserReview.setVisibility(View.GONE);
                            }else {
                                reviewData = items.get(0);
                                reviewId = items.get(0).getId_review();
                                getUserReview.setVisibility(View.VISIBLE);
                                postUserReview.setVisibility(View.GONE);

                                TextView username, rating, reviewText, time;
                                username = findViewById(R.id.tv_users_username);
                                rating = findViewById(R.id.tv_users_rating);
                                reviewText = findViewById(R.id.tv_users_review);
                                time = findViewById(R.id.tv_users_time);

                                username.setText("You");
                                rating.setText(String.valueOf(items.get(0).getRating()));
                                reviewText.setText(items.get(0).getReview_text());
                                time.setText(items.get(0).getUpdated_at().split("T")[0]);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ReviewResponse> call, Throwable t) {

                    }
                }
            );
    }

    @Override
    public void onClick(View v) {
        // --- Bookmark Button ---
        if (v.getId() == R.id.bookmark_btn) {
            showBookmarkDialog();
        }
        // --- Back Button ---
        else if (v.getId() == R.id.back_btn) {
            finish();
        }
        // --- Submit Review Button ---
        else if (v.getId() == R.id.tv_posting) {
            String reviewText = etUlasan.getText().toString().trim();
            int rating = (int) ratingBar.getRating();

            // Validasi
            if (reviewText.isEmpty() || rating == 0) {
                Toast.makeText(this, "Isi ulasan tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            // request JSON body
            JsonObject reviewBody = new JsonObject();
            reviewBody.addProperty("review_text", reviewText);
            reviewBody.addProperty("rating", rating);

            idComic = getIntent().getStringExtra("id_comic");
            postNewReview(reviewBody);
        }
        // --- Delete Review ---
        else if (v.getId() == R.id.delete_user_review) {
            deleteUserReview();
        }
        // --- Edit Review
        else if (v.getId() == R.id.edit_user_review) {
            editUserReview();
            tvPosting.setVisibility(View.GONE);
            updateBtn.setVisibility(View.VISIBLE);
        }
    }

    private void editUserReview() {
        postUserReview.setVisibility(View.VISIBLE);
        getUserReview.setVisibility(View.GONE);

        etUlasan.setText(reviewData.getReview_text());
        ratingBar.setRating((float) reviewData.getRating());

        TextView updateBtn = findViewById(R.id.tv_update);
        updateBtn.setOnClickListener(v -> {
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("rating", (int) ratingBar.getRating());
            jsonBody.addProperty("review_text", etUlasan.getText().toString().trim());

            apiService.updateUserReview("Bearer "+token, reviewId, jsonBody)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null && response.body() != null) {
                            String message = response.body().get("message").getAsString();
                            Toast.makeText(ComicDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
        });

    }

    private void deleteUserReview() {
        apiService.deleteUserReview("Bearer "+token, reviewId)
            .enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null && response.body() != null) {
                        String message = response.body().get("message").getAsString();
                        Toast.makeText(ComicDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
    }

    private void getReviewComics() {
        apiService.getComicReviews("Bearer "+token, idComic)
            .enqueue(new Callback<ReviewResponse>() {
                @Override
                public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body() != null) {
                        List<ReviewResponse.ReviewData> items = response.body().getData();

                        reviewList.clear();
                        reviewList.addAll(items);
                        adapter.notifyDataSetChanged();

                        double totalRating = 0;
                        int numberOfReviews = items.size();
                        tvComment.setText(Integer.toString(numberOfReviews));

                        if (numberOfReviews > 0) {
                            for (ReviewResponse.ReviewData review : items) {
                                totalRating += review.getRating();
                            }
                            double averageRating = totalRating / numberOfReviews;
                            tvAvgRating.setText(String.format("%.1f", averageRating));
                        } else {
                            tvAvgRating.setText("0.0");
                        }
                    }
                }

                 @Override
                 public void onFailure(Call<ReviewResponse> call, Throwable t) {
                     Log.e("ComicDetail", "Failed to get reviews: " + t.getMessage());
                     tvAvgRating.setText("N/A");
                 }
            }
        );
    }

    private void postNewReview(JsonObject reviewBody) {
        apiService.insertReview("Bearer "+token, idComic, reviewBody)
            .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null && response.body() != null) {
                            JsonObject resBody = response.body();

                            String message = resBody.get("message").getAsString();
                            Toast.makeText(ComicDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
            }
        );
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
                    insertBookmark(selectedStatus);
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


    private void insertBookmark(String selectedStatus) {
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
            }
        );
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
            }
        );
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
            }
        );
    }
}