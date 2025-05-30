package com.example.manchingu.fragments; // Ganti dengan nama package fragment Anda

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // Import ViewGroup
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.fragment.app.Fragment; // Ganti dari AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manchingu.R;
import com.example.manchingu.adapter.BookmarkAdapter; // Adapter untuk bookmark
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.BookmarkResponse;
import com.example.manchingu.activities.ComicDetailActivity; // Import Activity tujuan

// Hapus import BottomNavigationView dan NavigationBarView

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implementasikan View.OnClickListener dan BookmarkAdapter.OnComicClickListener
// Hapus implementasi BottomNavigationView listener
public class BookmarkFragment extends Fragment
        implements View.OnClickListener, BookmarkAdapter.OnComicClickListener {

    private RecyclerView rvBookmark;
    private SharedPreferences prefs;
    private List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    private BookmarkAdapter adapter;
    private ApiService apiService;
    private String token;
    private TextView CompletedBtn, ReadingBtn, DroppedBtn, PlanToReadBtn;
    // private BottomNavigationView bottomNav; // Hapus BottomNavigationView

    // Konstruktor kosong diperlukan
    public BookmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        // --- Inisialisasi Views ---
        // Cari Views di dalam 'view' yang diinflate
        rvBookmark = view.findViewById(R.id.rvBookmark);
        CompletedBtn = view.findViewById(R.id.completed_btn);
        ReadingBtn = view.findViewById(R.id.reading_btn);
        DroppedBtn = view.findViewById(R.id.dropped_btn);
        PlanToReadBtn = view.findViewById(R.id.plan_to_read_btn);

        // --- Setup RecyclerView ---
        ShowBookmarkRecyclerView(); // Panggil method setup RecyclerView

        // Get Token dari SharedPrefereces
        // Bersinggungan: Akses SharedPreferences dari Fragment menggunakan getContext()
        prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        token = prefs.getString("token","");
        // Log ini tetap bisa digunakan
        Log.d("BookmarkFragment", "Token: " + token); // Ubah tag Log

        // Memanggil fungsi ApiClient.getApiService()
        // Bersinggungan: Gunakan getContext() untuk ApiClient
        apiService = ApiClient.getApiService(getContext());

        // Panggil API untuk status default (misal: COMPLETED)
        // Bersinggungan: Panggil method API fetch data di onCreateView/onViewCreated
        // Pastikan token tidak kosong sebelum memanggil API
        if (token != null && !token.isEmpty()) {
            GetBookmark("COMPLETED");
            // Atur tampilan tombol default setelah data pertama dimuat
            if (getContext() != null) { // Tambahkan null check
                CompletedBtn.setBackgroundResource(R.drawable.rounded);
                CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.backgroundBtn));
            }
        } else {
            Log.e("BookmarkFragment", "Token is empty or null.");
            // Handle case where token is missing
            if (getContext() != null) {
                Toast.makeText(getContext(), "Token tidak ditemukan, mohon login kembali.", Toast.LENGTH_SHORT).show();
            }
        }


        // SetOnClickListener
        CompletedBtn.setOnClickListener(this);
        ReadingBtn.setOnClickListener(this);
        DroppedBtn.setOnClickListener(this);
        PlanToReadBtn.setOnClickListener(this);

        // Hapus inisialisasi dan listener untuk bottomNav di sini
        // bottomNav = view.findViewById(R.id.bottomNavigation);
        // bottomNav.setSelectedItemId(R.id.nav_bookmark); // Ini akan ditangani di Activity host
        // bottomNav.setOnNavigationItemSelectedListener(this); // Ini akan ditangani di Activity host

        return view; // Mengembalikan root view dari fragment layout
    }

    // Method Recycler view (Logika setup RecyclerView tetap sama)
    private void ShowBookmarkRecyclerView() {
        // Bersinggungan: Gunakan getContext() untuk LayoutManager dan Adapter
        if (getContext() != null) {
            rvBookmark.setLayoutManager(new GridLayoutManager(getContext(), 2));
            // Bersinggungan: Pass 'this' (Fragment) sebagai listener OnComicClickListener
            adapter = new BookmarkAdapter(getContext(), comicList, this);
            rvBookmark.setAdapter(adapter);
        } else {
            Log.e("BookmarkFragment", "Context is null when showing RecyclerView.");
        }
    }

    // Method untuk memanggil API bookmark berdasarkan status (Logika ini tetap sama)
    private void GetBookmark(String status) {
        // Bersinggungan: Panggilan API sama, tetapi perlu null check pada apiService, token, dan getContext()
        if (apiService == null || token == null || token.isEmpty() || getContext() == null) {
            Log.e("BookmarkFragment", "Cannot fetch bookmarks. ApiService, Token, or Context is null/empty.");
            // Bersihkan list jika gagal
            comicList.clear();
            if (adapter != null) adapter.notifyDataSetChanged();
            if (getContext() != null) {
                // Tampilkan pesan error jika context tersedia
                String errorMessage = "Gagal memuat bookmark. ";
                if (token == null || token.isEmpty()) {
                    errorMessage += "Token tidak valid.";
                } else {
                    errorMessage += "Terjadi kesalahan.";
                }
                // Hanya tampilkan toast jika fragment terhubung ke activity
                if (isAdded()) Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
            return;
        }


        // memanggil method ApiService.getAllMyBookmark dengan token user
        apiService.getMyBookmark("Bearer "+token, status)
                .enqueue(new Callback<BookmarkResponse>() {
                             @Override
                             public void onResponse(@NonNull Call<BookmarkResponse> call, @NonNull Response<BookmarkResponse> response) {
                                 // Pastikan Fragment masih attached sebelum update UI
                                 // Bersinggungan: Tambahkan isAdded() check sebelum mengakses context atau update UI
                                 if (!isAdded() || getContext() == null) {
                                     return;
                                 }

                                 if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                     comicList.clear();
                                     for (BookmarkResponse.Data bookmark : response.body().getData()) {
                                         comicList.add(bookmark.getComic());
                                     }
                                     // Bersinggungan: Pastikan adapter tidak null sebelum notifyDataSetChanged
                                     if (adapter != null) adapter.notifyDataSetChanged();
                                     else Log.w("BookmarkFragment", "Bookmark Adapter is null");

                                 } else {
                                     // Handle respon gagal (misalnya, status code 404, 500, dll.)
                                     // Bersinggungan: Gunakan getContext() untuk Toast
                                     String errorMessage = "Gagal memuat bookmark: " + response.code();
                                     // Hanya tampilkan toast jika fragment terhubung ke activity
                                     if (isAdded()) Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();

                                     // Kosongkan list jika gagal
                                     comicList.clear();
                                     // Bersinggungan: Pastikan adapter tidak null
                                     if (adapter != null) adapter.notifyDataSetChanged();
                                 }
                             }

                             @Override
                             public void onFailure(@NonNull Call<BookmarkResponse> call, @NonNull Throwable t) {
                                 // Pastikan Fragment masih attached
                                 // Bersinggungan: Tambahkan isAdded() check sebelum mengakses context atau update UI
                                 if (!isAdded() || getContext() == null) {
                                     return;
                                 }

                                 // Bersinggungan: Gunakan getContext() untuk Toast
                                 String errorMessage = "Error koneksi saat memuat bookmark: " + t.getMessage();
                                 // Hanya tampilkan toast jika fragment terhubung ke activity
                                 if (isAdded()) Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                 t.printStackTrace(); // Log error untuk debugging

                                 // Kosongkan list jika error
                                 comicList.clear();
                                 // Bersinggungan: Pastikan adapter tidak null
                                 if (adapter != null) adapter.notifyDataSetChanged();
                             }
                         }
                );
    }

    // --- Implementasi View.OnClickListener (untuk tombol status) ---
    @Override
    public void onClick(View v) {
        // Bersinggungan: Gunakan getContext() untuk ContextCompat
        if (getContext() == null) return;

        // Reset background semua tombol
        CompletedBtn.setBackground(null);
        ReadingBtn.setBackground(null);
        DroppedBtn.setBackground(null);
        PlanToReadBtn.setBackground(null);

        // Tentukan tombol yang diklik dan panggil GetBookmark()
        int id = v.getId();
        if (id == R.id.completed_btn){
            GetBookmark("COMPLETED");
            CompletedBtn.setBackgroundResource(R.drawable.rounded);
            CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.backgroundBtn));
        } else if (id == R.id.dropped_btn) {
            GetBookmark("DROPPED");
            DroppedBtn.setBackgroundResource(R.drawable.rounded);
            DroppedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.backgroundBtn));
        } else if (id == R.id.reading_btn) {
            GetBookmark("READING");
            ReadingBtn.setBackgroundResource(R.drawable.rounded);
            ReadingBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.backgroundBtn));
        } else if (id == R.id.plan_to_read_btn) {
            GetBookmark("PLAN_TO_READ");
            PlanToReadBtn.setBackgroundResource(R.drawable.rounded);
            PlanToReadBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.backgroundBtn));
        }
    }

    // --- Implementasi BookmarkAdapter.OnComicClickListener ---
    // Metode ini dipanggil saat item komik di RecyclerView diklik
    @Override
    public void onComicClick(BookmarkResponse.Comic comic) {
        // Logika navigasi ke detail komik ketika item diklik
        // Bersinggungan: Menggunakan getContext() untuk membuat Intent
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), ComicDetailActivity.class);
            // Memasukkan data komik ke dalam Intent sebagai extra (logika ini tetap sama)
            intent.putExtra("title", comic.getName());
            intent.putExtra("author", comic.getAuthor());
            intent.putExtra("artist", comic.getArtist());
            intent.putExtra("poster", comic.getPoster());
            intent.putExtra("synopsis", comic.getSynopsis());
            intent.putExtra("id_comic", comic.getId_comic());
            // Pastikan comic.getGenre() tidak null sebelum dimasukkan ke ArrayListExtra
            if (comic.getGenre() != null) {
                intent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
            } else {
                intent.putStringArrayListExtra("genre", new ArrayList<>()); // Kirim list kosong jika null
            }

            startActivity(intent);
            // Hapus finish() karena ini Fragment
            // finish();
        } else {
            Log.e("BookmarkFragment", "Context is null when trying to open detail.");
        }
    }

    // Optional: Bersihkan referensi View di onDestroyView
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvBookmark = null;
        CompletedBtn = null;
        ReadingBtn = null;
        DroppedBtn = null;
        PlanToReadBtn = null;
        adapter = null;
        // apiService, prefs, comicList tidak perlu di-null karena bukan View references
    }

    // Hapus implementasi onNavigationItemSelected karena ditangani di Activity host
    // @Override
    // public boolean onNavigationItemSelected(@NonNull MenuItem item) { ... }
}
