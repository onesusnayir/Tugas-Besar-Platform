package com.example.manchingu.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    // Input
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;

    // Button
    private Button submitBtn;
    private TextView loginBtn;
    private ImageView passIsShownBtn, passConfirmIsShownBtn;

    // Api Clinet
    private ApiService apiService;

    // Loading
    private ProgressBar progressBar;

    // Variabels
    private boolean isPassVisible = false;
    private boolean isConfirmPassVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi
        // Input
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm_password);

        // button
        submitBtn = findViewById(R.id.submit_btn);
        loginBtn = findViewById(R.id.login_btn);
        passIsShownBtn = findViewById(R.id.pass_is_shown);
        passConfirmIsShownBtn = findViewById(R.id.pass_confirm_is_shown);

        // loading
        progressBar = findViewById(R.id.progressBar);

        // Get Api Client
        apiService = ApiClient.getApiService(this);

        // OnClickListener
        submitBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        passIsShownBtn.setOnClickListener(this);
        passConfirmIsShownBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Register Button OnClick: Post ke API
        if (v.getId() == R.id.submit_btn){
            progressBar.setVisibility(View.VISIBLE);
            submitBtn.setEnabled(false);
            registerUser();
        }
        // Login Button onClick: Intent Login Activity
        else if (v.getId() == R.id.login_btn) {
            Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(login);
        }
        // PassIsShown Button OnClick: Menampilkan Password
        else if (v.getId() == R.id.pass_is_shown) {
            // Untuk Font
            Typeface typeface = etPassword.getTypeface();
            int selection = etPassword.getSelectionStart();
            if (isPassVisible) {
                // Ubah ke mode sembunyi password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passIsShownBtn.setImageResource(R.drawable.eye_alt);
                isPassVisible = !isPassVisible;
            } else {
                // Ubah ke mode tampilkan password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passIsShownBtn.setImageResource(R.drawable.eye_closed);
                isPassVisible = !isPassVisible;
            }
            // Untuk Font
            etPassword.setTypeface(typeface);
            etPassword.setSelection(selection);
        }
        // PassConfirmIsShown Button OnClick: Menampilkan Password
        else if (v.getId() == R.id.pass_confirm_is_shown) {
            // Untuk Font
            Typeface typeface = etConfirmPassword.getTypeface();
            int selection = etConfirmPassword.getSelectionStart();
            if (isConfirmPassVisible) {
                // Ubah ke mode sembunyi password
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passConfirmIsShownBtn.setImageResource(R.drawable.eye_alt);
                isConfirmPassVisible = !isConfirmPassVisible;
            } else {
                // Ubah ke mode tampilkan password
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passConfirmIsShownBtn.setImageResource(R.drawable.eye_closed);
                isConfirmPassVisible = !isConfirmPassVisible;
            }
            // Untuk Font
            etConfirmPassword.setTypeface(typeface);
            etConfirmPassword.setSelection(selection);
        }
    }

    private void registerUser() {
        // Ambil input user
        String username = etUsername.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validasi
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(password.equals(confirmPassword))){
            Toast.makeText(this, "Confirm password berbeda", Toast.LENGTH_SHORT).show();
        }

        // Buat User baru untuk request
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);

        // Memanggil method ApiService.createUser()
        Call<UserResponse> call = apiService.createUser(newUser);
        call.enqueue(new Callback<UserResponse>(){
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                // Response 200
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse res = response.body();
                    Toast.makeText(RegisterActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();

                    // Matikan Loading & Nyalakan Button
                    progressBar.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);
                    Log.d("REGISTER_SUCCESS", "Token: " + res.getToken());

                    // Pindah Ke Halaman Login
                    Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(login);
                } else {
                    // Matikan Loading & Nyalakan Button
                    progressBar.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);

                    // Show Cannot Register Message
                    Toast.makeText(RegisterActivity.this, "Gagal mendaftar", Toast.LENGTH_SHORT).show();
                    Log.e("REGISTER_FAIL", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Matikan Loading dan Nyalakan Button
                progressBar.setVisibility(View.GONE);
                submitBtn.setEnabled(true);

                // Show Error
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("REGISTER_ERROR", t.getMessage(), t);
            }
        });
    }
}