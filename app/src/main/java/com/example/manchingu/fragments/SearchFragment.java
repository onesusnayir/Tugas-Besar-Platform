package com.example.manchingu.fragments; // Ganti dengan nama package fragment Anda

import android.content.Context; // Import Context
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent; // Import KeyEvent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // Import ViewGroup
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager; // Import InputMethodManager
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment; // Ganti dari AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;
import com.example.manchingu.adapter.SearchAdapter; // Adapter untuk hasil pencarian
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse; // Model data dari API
import com.example.manchingu.activities.ComicDetailActivity; // Import Activity tujuan

// Hapus import BottomNavigationView dan PopupMenu
// import com.google.android.material.bottomnavigation.BottomNavigationView;
// import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implementasikan SearchAdapter.OnComicClickListener dan View.OnClickListener (jika ada view yang perlu click)
// Hapus implementasi BottomNavigationView listener
public class SearchFragment extends Fragment
        implements SearchAdapter.OnComicClickListener { // View.OnClickListener dihapus karena tidak ada tombol klik selain editTextSearch action

    // private BottomNavigationView bottomNavigationView; // Hapus BottomNavigationView
    private EditText editTextSearch;
    // private Button buttonFilter; // Dihapus dari Activity
    private RecyclerView rvSearch;
    private SearchAdapter searchAdapter;
    private ApiService apiService;

    // Konstruktor kosong diperlukan
    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // --- Inisialisasi Views ---
        // bottomNavigationView = view.findViewById(R.id.bottomNavigation); // Hapus
        editTextSearch = view.findViewById(R.id.editTextSearch);
        // buttonFilter = view.findViewById(R.id.buttonFilter); // Dihapus
        rvSearch = view.findViewById(R.id.rvSearch);

        // --- Inisialisasi RecyclerView ---
        // Bersinggungan: Gunakan getContext() sebagai context untuk adapter
        if (getContext() != null) { // Tambahkan null check
            searchAdapter = new SearchAdapter(getContext(), new ArrayList<>(), this); // 'this' sebagai listener
            // Pilih LayoutManager sesuai desain item_comic_grid.xml Anda
            // Bersinggungan: Gunakan getContext() untuk LayoutManager
            rvSearch.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Untuk grid 2 kolom

            rvSearch.setAdapter(searchAdapter);
        } else {
            Log.e("SearchFragment", "Context is null when setting up RecyclerView.");
            // Handle error, maybe show an empty state or error message
        }


        // --- Inisialisasi API Service ---
        // Bersinggungan: Gunakan getContext() saat memanggil getApiService()
        if (getContext() != null) { // Tambahkan null check
            apiService = ApiClient.getApiService(getContext());
        } else {
            Log.e("SearchFragment", "Context is null when initializing ApiService.");
            // Handle error, API calls will fail later
        }


        // --- Set Listener untuk Bottom Navigation ---
        // Hapus set listener bottom navigation karena ditangani di Activity host
        // bottomNavigationView.setOnNavigationItemSelectedListener(this);
        // bottomNavigationView.setSelectedItemId(R.id.nav_search); // Ini akan ditangani di Activity host

        // --- Set Listener untuk Search EditText ---
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                // Bersinggungan: Memanggil hideKeyboard dari Fragment
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        // --- Set OnClickListener untuk Filter Button ---
        // buttonFilter.setOnClickListener(v -> showFilterMenu(v)); // Dihapus

        return view; // Mengembalikan root view dari fragment layout
    }

    // --- Method untuk Melakukan Pencarian API ---
    private void performSearch() {
        String query = editTextSearch.getText().toString().trim();
        // Bersinggungan: Gunakan getContext() untuk Toast
        if (query.isEmpty()) {
            if (getContext() != null) Toast.makeText(getContext(), "Masukkan judul komik untuk dicari", Toast.LENGTH_SHORT).show();
            // Bersinggungan: Pastikan adapter tidak null sebelum update data
            if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan hasil sebelumnya
            return;
        }

        // Bersinggungan: Perlu null check pada apiService dan getContext()
        if (apiService == null || getContext() == null) {
            Log.e("SearchFragment", "ApiService or Context is null, cannot perform search.");
            if (getContext() != null) {
                // Hanya tampilkan toast jika fragment terhubung ke activity
                if (isAdded()) Toast.makeText(getContext(), "Aplikasi sedang memuat, coba lagi nanti.", Toast.LENGTH_SHORT).show();
            }
            if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan hasil sebelumnya
            return;
        }


        // Tampilkan indikator loading (opsional)

        apiService.getComicByTitle(query).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // Sembunyikan indikator loading (opsional)

                // Pastikan Fragment masih attached sebelum update UI
                // Bersinggungan: Tambahkan isAdded() check sebelum mengakses context atau update UI
                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ComicResponse comicResponse = response.body();

                    ComicResponse.Data data = comicResponse.getData();
                    List<ComicResponse.Item> comicList = new ArrayList<>();

                    if (data != null && data.getItems() != null) {
                        comicList = data.getItems(); // Ambil list item dari objek Data
                    }

                    if (!comicList.isEmpty()) {
                        // Bersinggungan: Pastikan adapter tidak null sebelum update data
                        if (searchAdapter != null) searchAdapter.updateData(comicList); // Update adapter dengan data dari API
                        else Log.w("SearchFragment", "Search Adapter is null in onResponse success.");
                    } else {
                        // Bersinggungan: Gunakan getContext() untuk Toast
                        if (getContext() != null) Toast.makeText(getContext(), "Komik dengan judul \"" + query + "\" tidak ditemukan", Toast.LENGTH_SHORT).show();
                        // Bersinggungan: Pastikan adapter tidak null
                        if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView jika tidak ada hasil
                        else Log.w("SearchFragment", "Search Adapter is null in onResponse empty.");
                    }

                } else {
                    // Handle respon gagal (misalnya, status code 404, 500, dll.)
                    // Bersinggungan: Gunakan getContext() untuk Toast
                    if (getContext() != null) Toast.makeText(getContext(), "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Bersinggungan: Pastikan adapter tidak null
                    if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView
                    else Log.w("SearchFragment", "Search Adapter is null in onResponse failure.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // Sembunyikan indikator loading (opsional)

                // Pastikan Fragment masih attached
                // Bersinggungan: Tambahkan isAdded() check sebelum mengakses context atau update UI
                if (!isAdded() || getContext() == null) {
                    return;
                }

                // Bersinggungan: Gunakan getContext() untuk Toast
                if (getContext() != null) Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                // Bersinggungan: Pastikan adapter tidak null
                if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView
                else Log.w("SearchFragment", "Search Adapter is null in onFailure.");
            }
        });
    }

    // Helper method untuk menyembunyikan keyboard
    // Bersinggungan: Menggunakan getContext() dan findViewById pada root view fragment
    private void hideKeyboard(View view) {
        if (getContext() == null || view == null) return;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // Menggunakan token dari view yang menyebabkan event (EditText)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // --- Implementasi OnComicClickListener dari SearchAdapter ---
    @Override
    public void onComicClick(ComicResponse.Item comic) {
        // Logika navigasi ke detail komik ketika item diklik
        // Bersinggungan: Menggunakan getContext() untuk membuat Intent
        if (getContext() == null) {
            Log.e("SearchFragment", "Context is null when trying to open detail.");
            return;
        }
        Intent detailIntent = new Intent(getContext(), ComicDetailActivity.class);

        // Memasukkan data komik ke dalam Intent sebagai extra (logika ini tetap sama)
        detailIntent.putExtra("id_comic", comic.getId_comic());
        detailIntent.putExtra("title", comic.getName());
        detailIntent.putExtra("author", comic.getAuthor());
        detailIntent.putExtra("artist", comic.getArtist());
        detailIntent.putExtra("poster", comic.getPoster());
        detailIntent.putExtra("synopsis", comic.getSynopsis());

        // Pastikan comic.getGenre() tidak null sebelum dimasukkan ke ArrayListExtra
        if (comic.getGenre() != null) {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
        } else {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>()); // Kirim list kosong jika null
        }

        // Memulai DetailComicActivity
        startActivity(detailIntent);
        // Hapus finish() karena ini Fragment
        // finish();
    }

    // Hapus implementasi onNavigationItemSelected karena ditangani di Activity host
    // @Override
    // public boolean onNavigationItemSelected(@NonNull MenuItem item) { ... }

    // Optional: Bersihkan referensi View di onDestroyView
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        editTextSearch = null;
        rvSearch = null;
        searchAdapter = null;
        // apiService tidak perlu di-null karena bukan View reference
    }
}
