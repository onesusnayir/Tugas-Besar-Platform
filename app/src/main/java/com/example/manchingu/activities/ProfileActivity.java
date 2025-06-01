package com.example.manchingu.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar; // Jika kamu ingin pakai ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.manchingu.R;
import com.example.manchingu.api.ApiClient; // Menggunakan ApiClient yang sudah ada
import com.example.manchingu.api.ApiService; // Menggunakan ApiService yang sudah ada
import com.example.manchingu.response.ProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView backBtn;
    TextView tvUsername;
    TextView tvEmail;
    // ProgressBar progressBar; // Deklarasikan jika ada di layout dan ingin digunakan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi Views
        backBtn = findViewById(R.id.back_btn);
        tvUsername = findViewById(R.id.tp_username);
        tvEmail = findViewById(R.id.tp_email);
        // progressBar = findViewById(R.id.progressBar); // Inisialisasi jika ada
        // if (progressBar != null) {
        //     progressBar.setVisibility(View.VISIBLE); // Tampilkan ProgressBar saat loading
        // }


        backBtn.setOnClickListener(this);

        // Panggil fetch data profil
        fetchProfileData();
    }

    private void fetchProfileData() {
        // Ambil ApiService dari ApiClient yang sudah kamu gunakan
        ApiService apiService = ApiClient.getApiService(this); // Menggunakan method yang menerima Context

        // Ambil token dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("auth_token", null);

        if (token == null) {
            Toast.makeText(this, "Autentikasi diperlukan. Silakan login kembali.", Toast.LENGTH_LONG).show();
            // Optional: Arahkan ke LoginActivity
            // Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            // startActivity(loginIntent);
            finish();
            return;
        }

        String fullToken = "Bearer " + token; // Sesuaikan format token jika diperlukan

        // Panggil API untuk mendapatkan profil
        apiService.getMyProfile(fullToken).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                // if (progressBar != null) {
                //     progressBar.setVisibility(View.GONE); // Sembunyikan ProgressBar
                // }
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess()) {
                        ProfileResponse.Data profileData = profileResponse.getData();
                        if (profileData != null) {
                            tvUsername.setText(profileData.getUsername());
                            tvEmail.setText(profileData.getEmail());
                        } else {
                            Toast.makeText(ProfileActivity.this, "Data profil tidak ditemukan.", Toast.LENGTH_SHORT).show();
                            Log.e("ProfileActivity", "API Success, but Data is null");
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Gagal memuat profil: " + profileResponse.isSuccess(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "API Response Success: false");
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Error respon dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("ProfileActivity", "Response not successful: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                // if (progressBar != null) {
                //     progressBar.setVisibility(View.GONE); // Sembunyikan ProgressBar
                // }
                Toast.makeText(ProfileActivity.this, "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfileActivity", "API Call Failure: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_btn) {
            finish(); // Kembali ke Activity sebelumnya
        }
    }
}