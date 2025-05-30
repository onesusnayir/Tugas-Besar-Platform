package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.manchingu.R;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.model.User;
import com.example.manchingu.response.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail, etPassword;
    Button submitBtn;
    ApiService apiService;
    TextView registerBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);

        submitBtn = findViewById(R.id.submit_btn);
        registerBtn = findViewById(R.id.register_btn);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiClient.getApiService(this);

        // OnClickListener
        submitBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Login Button OnClick: Post ke API
        if (v.getId() == R.id.submit_btn){
            progressBar.setVisibility(View.VISIBLE);
            submitBtn.setEnabled(false); // Nonaktifkan tombol
            loginUser();
        }
        // Register Button OnClick: Intent Register Activity
        else if(v.getId() == R.id.register_btn){
            Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(register);
        }
    }

    private void loginUser() {
        // Get Email and Password
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        // New user untuk request
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        // Memanggil method ApiService.login
        Call<UserResponse> call = apiService.login(user);

        // Menjalankan request secara asynchronous
        call.enqueue(new Callback<UserResponse>(){
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse res = response.body();
                    Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("LOGIN_SUCCESS", "Token: " + res.getToken());
                    progressBar.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);

                    // Simpan Token dan Username
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", res.getData().getUsername());
                    editor.putString("token", res.getToken());
                    editor.apply();

                    Intent home = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(home);
                } else {
                    progressBar.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Gagal Login", Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN_FAIL", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                submitBtn.setEnabled(true);
                Log.e("LOGIN_ERROR", t.getMessage(), t);
            }
        });
    }
}