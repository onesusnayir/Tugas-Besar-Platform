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
import android.widget.ProgressBar;
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

// Implementasikan SearchAdapter.OnComicClickListener dan View.OnClickListener
public class SearchFragment extends Fragment
        implements SearchAdapter.OnComicClickListener {
    // Input
    private EditText editTextSearch;

    // Recycler View
    private RecyclerView rvSearch;
    private SearchAdapter searchAdapter;

    // Api Client
    private ApiService apiService;

    // Loading
    private ProgressBar progressBar;

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
        editTextSearch = view.findViewById(R.id.editTextSearch);
        rvSearch = view.findViewById(R.id.rvSearch);

        // --- Inisialisasi Loading
        progressBar = view.findViewById(R.id.progressBar);

        // --- Inisialisasi RecyclerView ---
        if (getContext() != null) {
            searchAdapter = new SearchAdapter(getContext(), new ArrayList<>(), this);
            rvSearch.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvSearch.setAdapter(searchAdapter);
        } else {
            Log.e("SearchFragment", "Context is null when setting up RecyclerView.");
        }


        // --- Inisialisasi API Service ---
        if (getContext() != null) {
            apiService = ApiClient.getApiService(getContext());
        } else {
            Log.e("SearchFragment", "Context is null when initializing ApiService.");

        }

        // --- Set Listener untuk Search EditText ---
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                progressBar.setVisibility(View.VISIBLE);
                performSearch();
                // Memanggil hideKeyboard dari Fragment
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        // Mengembalikan root view dari fragment layout
        return view;
    }

    // --- Method untuk Melakukan Pencarian API ---
    private void performSearch() {
        String query = editTextSearch.getText().toString().trim();
        if (query.isEmpty()) {
            if (getContext() != null) Toast.makeText(getContext(), "Masukkan judul komik untuk dicari", Toast.LENGTH_SHORT).show();
            if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>());
            return;
        }

        // Cek Api Client & Context
        if (apiService == null || getContext() == null) {
            Log.e("SearchFragment", "ApiService or Context is null, cannot perform search.");
            if (getContext() != null) {
                if (isAdded()) Toast.makeText(getContext(), "Aplikasi sedang memuat, coba lagi nanti.", Toast.LENGTH_SHORT).show();
            }
            // Kosongkan hasil sebelumnya
            if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>());
            return;
        }

        apiService.getComicByTitle(query).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // Matikan Loading
                progressBar.setVisibility(View.GONE);

                // Pastikan Fragment masih attached sebelum update UI
                if (!isAdded() || getContext() == null) {
                    return;
                }

                // Kode 200
                if (response.isSuccessful() && response.body() != null) {
                    ComicResponse comicResponse = response.body();

                    ComicResponse.Data data = comicResponse.getData();
                    List<ComicResponse.Item> comicList = new ArrayList<>();

                    // Ambil list item dari objek Data
                    if (data != null && data.getItems() != null) {
                        comicList = data.getItems();
                    }

                    if (!comicList.isEmpty()) {
                        // Update adapter dengan data dari API
                        if (searchAdapter != null) searchAdapter.updateData(comicList);
                        else Log.w("SearchFragment", "Search Adapter is null in onResponse success.");
                    } else {
                        if (getContext() != null) Toast.makeText(getContext(), "Komik dengan judul \"" + query + "\" tidak ditemukan", Toast.LENGTH_SHORT).show();

                        // Kosongkan RecyclerView jika tidak ada hasil
                        if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>());
                        else Log.w("SearchFragment", "Search Adapter is null in onResponse empty.");
                    }

                } else {
                    // Handle respon gagal (status code 404, 500, dll.)
                    if (getContext() != null) Toast.makeText(getContext(), "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();

                    if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView
                    else Log.w("SearchFragment", "Search Adapter is null in onResponse failure.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // Sembunyikan indikator loading
                progressBar.setVisibility(View.GONE);

                // Pastikan Fragment masih attached
                if (!isAdded() || getContext() == null) {
                    return;
                }


                if (getContext() != null) Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                // Pastikan adapter tidak null
                if (searchAdapter != null) searchAdapter.updateData(new ArrayList<>()); // Kosongkan RecyclerView
                else Log.w("SearchFragment", "Search Adapter is null in onFailure.");
            }
        });
    }

    // Helper method untuk menyembunyikan keyboard
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
        detailIntent.putExtra("status",comic.getStatus());
        detailIntent.putExtra("bookmarked",comic.getBookmarked());

        // Pastikan comic.getGenre() tidak null sebelum dimasukkan ke ArrayListExtra
        if (comic.getGenre() != null) {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
        } else {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>()); // Kirim list kosong jika null
        }

        // Memulai DetailComicActivity
        startActivity(detailIntent);
    }

    // Optional: Bersihkan referensi View di onDestroyView
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        editTextSearch = null;
        rvSearch = null;
        searchAdapter = null;
    }
}
