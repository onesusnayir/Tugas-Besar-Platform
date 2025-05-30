package com.example.manchingu.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button; // Button masih diperlukan jika ada button lain, tapi buttonFilter tidak
import android.widget.EditText;
import android.widget.PopupMenu; // PopupMenu masih diperlukan jika showFilterMenu dipertahankan untuk filter yang lain
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;
import com.example.manchingu.adapter.SearchAdapter;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        SearchAdapter.OnComicClickListener {

    private BottomNavigationView bottomNavigationView;
    private EditText editTextSearch;
    // private Button buttonFilter; // Dihapus
    private RecyclerView rvSearch;
    private SearchAdapter searchAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Inisialisasi Views ---
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        editTextSearch = findViewById(R.id.editTextSearch);
        // buttonFilter = findViewById(R.id.buttonFilter); // Dihapus
        rvSearch = findViewById(R.id.rvSearch);

        // --- Inisialisasi RecyclerView ---
        searchAdapter = new SearchAdapter(this, new ArrayList<>(), this); // Mulai dengan list kosong, 'this' sebagai context dan listener
        // Pilih LayoutManager sesuai desain item_comic_grid.xml Anda
        // rvSearch.setLayoutManager(new LinearLayoutManager(this)); // Untuk list 1 kolom
        rvSearch.setLayoutManager(new GridLayoutManager(this, 2)); // Untuk grid 2 kolom (sesuaikan jumlah kolom jika perlu)

        rvSearch.setAdapter(searchAdapter);

        // --- Inisialisasi API Service ---
        // Memberikan context (this) saat memanggil getApiService()
        apiService = ApiClient.getApiService(this);

        // --- Set Listener untuk Bottom Navigation ---
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Optional: Set item yang dipilih saat ini
        // bottomNavigationView.setSelectedItemId(R.id.nav_search);

        // --- Set Listener untuk Search EditText ---
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        // --- Set OnClickListener untuk Filter Button ---
        // buttonFilter.setOnClickListener(v -> showFilterMenu(v)); // Dihapus
    }

    // --- Method untuk Melakukan Pencarian API ---
    private void performSearch() {
        String query = editTextSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Masukkan judul komik untuk dicari", Toast.LENGTH_SHORT).show();
            searchAdapter.updateData(new ArrayList<>()); // Kosongkan hasil sebelumnya
            return;
        }

        // Tampilkan indikator loading (opsional)

        apiService.getComicByTitle(query).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // Sembunyikan indikator loading (opsional)

                if (response.isSuccessful() && response.body() != null) {
                    ComicResponse comicResponse = response.body();

                    ComicResponse.Data data = comicResponse.getData();
                    List<ComicResponse.Item> comicList = new ArrayList<>();

                    if (data != null && data.getItems() != null) {
                        comicList = data.getItems(); // Ambil list item dari objek Data
                    }

                    if (!comicList.isEmpty()) {
                        searchAdapter.updateData(comicList); // Update adapter dengan data dari API
                    } else {
                        Toast.makeText(SearchActivity.this, "Komik dengan judul \"" + query + "\" tidak ditemukan", Toast.LENGTH_SHORT).show();
                        searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView jika tidak ada hasil
                    }

                } else {
                    // Handle respon gagal (misalnya, status code 404, 500, dll.)
                    Toast.makeText(SearchActivity.this, "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();
                    searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // Sembunyikan indikator loading (opsional)
                Toast.makeText(SearchActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView
            }
        });
    }



    // Helper method untuk menyembunyikan keyboard (sama)
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // --- Implementasi OnComicClickListener dari SearchAdapter ---
    @Override
    public void onComicClick(ComicResponse.Item comic) {
        // Mengganti Toast dengan membuka ComicDetailActivity dan passing data
        Intent detailIntent = new Intent(this, ComicDetailActivity.class);

        // Memasukkan data komik ke dalam Intent sebagai extra
        // Pastikan nama key ("id_comic", "title", dll.) sesuai dengan yang diterima di ComicDetailActivity
        detailIntent.putExtra("id_comic", comic.getId_comic()); // Menggunakan ID unik
        detailIntent.putExtra("title", comic.getName());
        detailIntent.putExtra("author", comic.getAuthor());
        detailIntent.putExtra("poster", comic.getPoster());
        detailIntent.putExtra("synopsis", comic.getSynopsis());
        detailIntent.putExtra("artist", comic.getArtist());
        detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));


        // Jika Anda perlu passing data lain (misalnya genre atau rating), tambahkan di sini
        // detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
        // detailIntent.putExtra("rating", comic.getRating());

        // Memulai DetailComicActivity
        startActivity(detailIntent);
    }

    // --- Logika Bottom Navigation (sama) ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_bookmark){
            Intent intent = new Intent(SearchActivity.this, BookmarkActivity.class);
            startActivity(intent);
            // finish(); // Opsional: tutup activity saat ini
            return true;
        } else if (itemId == R.id.nav_home){
            Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
            startActivity(intent);
            // finish(); // Opsional: tutup activity saat ini
            return true;
        }
        // Jika ada item search di menu dan diklik saat sudah di SearchActivity
        // if (itemId == R.id.nav_search) {
        //     // Already in SearchActivity, maybe scroll to top or just do nothing
        //     return true;
        // }
        return false; // Item click not handled
    }

    // Optional: Override onResume (sama)
    // @Override
    // protected void onResume() {
    //     super.onResume();
    //     // bottomNavigationView.setSelectedItemId(R.id.nav_search);
    // }
}
