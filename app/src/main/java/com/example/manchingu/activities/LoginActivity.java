package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);

        submitBtn = findViewById(R.id.submit_btn);
        registerBtn = findViewById(R.id.register_btn);
        submitBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);

        apiService = ApiClient.getApiService(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submit_btn){
            loginUser();
        }else if(v.getId() == R.id.register_btn){
            Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(register);
        }
    }

    private void loginUser() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        Call<UserResponse> call = apiService.login(user);
        call.enqueue(new Callback<UserResponse>(){
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse res = response.body();
                    Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("LOGIN_SUCCESS", "Token: " + res.getToken());

                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", res.getData().getUsername());
                    editor.apply();

                    Intent home = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(home);
                } else {
                    Toast.makeText(LoginActivity.this, "Gagal mendaftar", Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN_FAIL", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LOGIN_ERROR", t.getMessage(), t);
            }
        });
    }
}