package com.example.manchingu.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    // Button
    ImageView backBtn;
    Button logoutBtn;

    // View
    TextView tvUsername;
    TextView tvEmail;
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
        logoutBtn = findViewById(R.id.logout_button);
        tvUsername = findViewById(R.id.tp_username);
        tvEmail = findViewById(R.id.tp_email);

        // Button Click Listener
        backBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);

        // Panggil fetch data profil
        fetchProfileData();
    }
    // OnClick Fuction
    @Override
    public void onClick(View v) {
        // Kembali ke Activity sebelumnya
        if (v.getId() == R.id.back_btn) {
            finish();
        }
        // Logout
        else if (v.getId() == R.id.logout_button) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Kembali Ke Halaman Login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Menghapus backstack
            startActivity(intent);
        }
    }

    // Fetch Data Profile User
    private void fetchProfileData() {
        ApiService apiService = ApiClient.getApiService(this);

        // Ambil token dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Autentikasi diperlukan. Silakan login kembali.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bearer Authentication
        String fullToken = "Bearer " + token;

        // Panggil API untuk mendapatkan profil
        apiService.getMyProfile(fullToken).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();

                    // Response 200
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
                Toast.makeText(ProfileActivity.this, "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProfileActivity", "API Call Failure: " + t.getMessage(), t);
            }
        });
    }
}