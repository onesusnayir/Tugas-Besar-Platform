package com.example.manchingu.fragments; // Ganti dengan nama package fragment Anda

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // Import ViewGroup
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Import Toast
import android.widget.ImageView; // Import ImageView


import androidx.annotation.NonNull; // Import
import androidx.fragment.app.Fragment; // Ganti dari AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2; // Import ViewPager2

import com.example.manchingu.R;
import com.example.manchingu.activities.ProfileActivity;
import com.example.manchingu.adapter.AllComicAdapter; // Adapter untuk rekomendasi
import com.example.manchingu.adapter.BannerPagerAdapter; // Adapter untuk banner carousel
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse; // Model data dari API
import com.example.manchingu.activities.AllComicsActivity; // Import Activity tujuan
import com.example.manchingu.activities.ComicDetailActivity; // Import Activity tujuan
import com.example.manchingu.response.ProfileResponse;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implementasikan View.OnClickListener, BannerPagerAdapter.OnComicClickListener, dan AllComicAdapter.OnComicClickListener

public class HomeFragment extends Fragment
        implements View.OnClickListener,
        BannerPagerAdapter.OnComicClickListener,
        AllComicAdapter.OnComicClickListener {

    SharedPreferences prefs;
    TextView tvUsername;
    TextView seeComicsBtn;
    ImageView profileImage;

    // --- Untuk Banner Carousel ---
    private ViewPager2 bannerViewPager;
    private BannerPagerAdapter bannerAdapter;
    // Inisialisasi di onViewCreated
    private Handler sliderHandler;
    // Inisialisasi di onViewCreated
    private Runnable sliderRunnable;
    private final long SLIDER_DELAY = 3000; // Delay dalam milidetik (3 detik) antar slide
    private String userEmail;
    private String userActualUsername;
    // --- Untuk RecyclerView Rekomendasi ---
    RecyclerView rvRekomendasi;
    AllComicAdapter adapter;
    List<ComicResponse.Item> fetchedComicItems = new ArrayList<>();

    ApiService apiService;

    // Loading
    ProgressBar progressBarBanner, progressBarComics;

    // Konstruktor kosong diperlukan untuk Fragment
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cari Views di dalam 'view' yang diinflate
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Inisialisasi ---
        prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", "defaultName");
        tvUsername = view.findViewById(R.id.username);
        tvUsername.setText(username);

        profileImage = view.findViewById(R.id.profile);
        seeComicsBtn = view.findViewById(R.id.see_all_btn);

        // Set listener klik untuk tombol Lihat Semua
        seeComicsBtn.setOnClickListener(this);
        profileImage.setOnClickListener(this);

        // --- Inisialisasi API Service ---
        apiService = ApiClient.getApiService(getContext());

        // --- Setup Banner ViewPager2 ---
        bannerViewPager = view.findViewById(R.id.bannerViewPager);

        // Set listener untuk adapter banner ke Fragment ini
        bannerAdapter = new BannerPagerAdapter(this);
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup Auto-Scrolling Banner Runnable (disederhanakan untuk infinite loop)
        // Inisialisasi handler dan runnable
        sliderHandler = new Handler(Looper.getMainLooper());

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerViewPager != null && bannerAdapter != null && bannerAdapter.getActualItemCount() > 1) {
                    bannerViewPager.setCurrentItem(bannerViewPager.getCurrentItem() + 1, true);
                    // Bersinggungan: Memanggil postDelayed pada handler yang diinisialisasi di Fragment
                    sliderHandler.postDelayed(this, SLIDER_DELAY);
                }
            }
        };

        // Hentikan auto-scroll saat user berinteraksi, lanjutkan saat berhenti
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (sliderHandler != null) { // Tambahkan null check
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        // Menghentikan Handler pada interaksi user
                        sliderHandler.removeCallbacks(sliderRunnable);
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        if (bannerAdapter != null && bannerAdapter.getActualItemCount() > 1) {
                            // Memulai kembali Handler
                            sliderHandler.removeCallbacks(sliderRunnable);
                            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                        }
                    }
                }
            }
        });

        // --- Setup RecyclerView Rekomendasi ---
        rvRekomendasi = view.findViewById(R.id.rvRekomendasi);
        adapter = new AllComicAdapter(getContext(), fetchedComicItems, this); // Pass 'this' karena fragment mengimplement OnComicClickListener

        rvRekomendasi.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRekomendasi.setAdapter(adapter);

        // --- Loading ---
        progressBarBanner = view.findViewById(R.id.progressBarBanner);
        progressBarComics = view.findViewById(R.id.progressBarComics);
        progressBarBanner.setVisibility(View.VISIBLE);
        progressBarBanner.setVisibility(View.VISIBLE);

        // --- Panggil API untuk mendapatkan data ---
        // Panggil method untuk fetch data dari API
        fetchComicsData();
        fetchProfileData();
        // Mengembalikan root view dari fragment layout
        return view;
    }

    // Mengelola Handler di onResume() dan onPause() Fragment
    @Override
    public void onResume() {
        super.onResume();
        // Mulai auto-scroll saat fragment aktif
        if (sliderHandler != null && sliderRunnable != null && bannerAdapter != null && bannerAdapter.getActualItemCount() > 1 && bannerViewPager != null && bannerViewPager.getVisibility() == View.VISIBLE) {
            sliderHandler.removeCallbacks(sliderRunnable);
            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
        }
        fetchComicsData();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hentikan auto-scroll saat fragment tidak terlihat
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Membersihkan Handler di onDestroyView() Fragment
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
        tvUsername = null;
        seeComicsBtn = null;
        profileImage = null;
        bannerViewPager = null;
        rvRekomendasi = null;
        adapter = null;
        bannerAdapter = null;
        apiService = null;
        prefs = null;
    }


    // Method untuk memanggil API
    private void fetchComicsData() {
        // Check is null pada apiService dan getContext()
        if (apiService == null || getContext() == null) {
            Log.e("HomeFragment", "ApiService or Context is not initialized.");
            // Handle error, maybe show a message to the user
            if (getContext() != null) {
                Toast.makeText(getContext(), "Aplikasi sedang memuat, coba lagi nanti.", Toast.LENGTH_SHORT).show();
            }
            // Pastikan juga membersihkan adapter jika ada data sebelumnya yang usang
            fetchedComicItems.clear();
            if (adapter != null) adapter.notifyDataSetChanged();
            if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>());
            if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
            if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE);

            return;
        }

        // Panggil API getLimitedComics
        apiService.getLimitedComics(1, 10).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // Check Fragment masih attached dan isAdded() check
                if (!isAdded() || getContext() == null) {
                    return;
                }

                progressBarBanner.setVisibility(View.GONE);
                progressBarComics.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                    List<ComicResponse.Item> items = response.body().getData().getItems();

                    if (items != null && !items.isEmpty()) {
                        // Update data di list yang digunakan oleh adapter rekomendasi
                        fetchedComicItems.clear();
                        fetchedComicItems.addAll(items);
                        // Pastikan adapter tidak null
                        if (adapter != null) adapter.notifyDataSetChanged();
                        else Log.w("HomeFragment", "Adapter Rekomendasi is null");


                        // Update data di adapter banner dengan data yang sama
                        if (bannerAdapter != null) bannerAdapter.updateData(items); // Pastikan adapter tidak null
                        else Log.w("HomeFragment", "Banner Adapter is null");


                        // Jika ada lebih dari 1 item untuk carousel:
                        if (bannerAdapter != null && bannerAdapter.getActualItemCount() > 1) {
                            // Atur ViewPager2 ke posisi awal yang 'acak' di tengah Integer.MAX_VALUE
                            int initialPosition = Integer.MAX_VALUE / 2;
                            initialPosition = initialPosition - (initialPosition % bannerAdapter.getActualItemCount());

                            if (bannerViewPager != null) { // Pastikan ViewPager tidak null
                                bannerViewPager.setCurrentItem(initialPosition, false);

                                // Batalkan callback apa pun sebelum menjadwalkan
                                if (sliderHandler != null && sliderRunnable != null) {
                                    sliderHandler.removeCallbacks(sliderRunnable);
                                    // Beri sedikit delay sebelum slide otomatis pertama dimulai
                                    sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                                }
                                // Pastikan banner terlihat
                                bannerViewPager.setVisibility(View.VISIBLE);
                            }

                        } else {
                            // Jika 0 atau 1 item, hentikan auto-scroll dan sembunyikan banner
                            if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                            if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Bersinggungan: Pastikan ViewPager tidak null
                        }

                    } else {
                        // Handle jika list kosong tapi response sukses
                        Toast.makeText(getContext(), "Tidak ada data komik yang ditemukan.", Toast.LENGTH_SHORT).show();
                        // Kosongkan kedua adapter jika tidak ada data
                        fetchedComicItems.clear();
                        if (adapter != null) adapter.notifyDataSetChanged(); // Pastikan adapter tidak null
                        if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>()); // Pastikan adapter tidak null
                        // Hentikan auto-scroll dan sembunyikan banner
                        if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                        if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Pastikan ViewPager tidak null
                    }

                } else {
                    // Handle respon gagal (misalnya, status code 404, 500, dll.)
                    Toast.makeText(getContext(), "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Kosongkan kedua adapter jika gagal
                    fetchedComicItems.clear();
                    if (adapter != null) adapter.notifyDataSetChanged(); // Pastikan adapter tidak null
                    if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>()); // Pastikan adapter tidak null
                    // Hentikan auto-scroll dan sembunyikan banner
                    if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                    if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Pastikan ViewPager tidak null
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // Check Fragment masih attached dan isAdded()
                if (!isAdded() || getContext() == null) {
                    return;
                }

                Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace(); // Log error untuk debugging

                // Kosongkan kedua adapter jika error
                fetchedComicItems.clear();
                if (adapter != null) adapter.notifyDataSetChanged(); // Pastikan adapter tidak null
                if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>()); // Pastikan adapter tidak null
                // Hentikan auto-scroll dan sembunyikan banner
                if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Pastikan ViewPager tidak null
            }
        });
    }

    // --- Method baru untuk fetch data profil ---
    private void fetchProfileData() {
        if (apiService == null || getContext() == null) {
            Log.e("HomeFragment", "ApiService or Context for Profile is not initialized.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Aplikasi sedang memuat profil, coba lagi nanti.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE); // Ganti "user_prefs" dengan nama shared preference yang benar
        String token = sharedPreferences.getString("token", null); // Ganti "auth_token" dengan key yang kamu gunakan

        Log.d("Token", token);
        if (token == null) {
            Toast.makeText(getContext(), "Autentikasi diperlukan untuk profil. Silakan login kembali.", Toast.LENGTH_LONG).show();
            Log.e("HomeFragment", "Token not found for profile fetch.");
            return;
        }

        String fullToken = "Bearer " + token;

        apiService.getMyProfile(fullToken).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess()) {
                        ProfileResponse.Data profileData = profileResponse.getData();
                        if (profileData != null) {
                            userActualUsername = profileData.getUsername();
                            userEmail = profileData.getEmail();
                            tvUsername.setText(userActualUsername); // Update TextView username di HomeFragment

                            Log.d("HomeFragment", "Profile fetched: " + userActualUsername + ", " + userEmail);
                        } else {
                            Toast.makeText(getContext(), "Data profil kosong.", Toast.LENGTH_SHORT).show();
                            Log.e("HomeFragment", "API Success, but Profile Data is null");
                        }
                    } else {
                        Toast.makeText(getContext(), "Gagal memuat profil: " + response.message(), Toast.LENGTH_SHORT).show();
                        Log.e("HomeFragment", "API Response Success: false, Message: " + response.message());
                    }
                } else {
                    Toast.makeText(getContext(), "Error respon profil dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("HomeFragment", "Profile Response not successful: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) {
                    return;
                }

                Toast.makeText(getContext(), "Gagal terhubung untuk profil: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Profile API Call Failure: " + t.getMessage(), t);
            }
        });
    }
    // --- Implementasi View.OnClickListener (untuk tombol Lihat Semua) ---
    @Override
    public void onClick(View v) {
        // Intent Ke All Comic Activity
        if(v.getId() == R.id.see_all_btn && getContext() != null){
            Intent intent = new Intent(getContext(), AllComicsActivity.class);
            startActivity(intent);
        }else if (v.getId() == R.id.profile && getContext() != null) {
            // Ketika tombol profil diklik, kirim data yang sudah di-fetch
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra("username", userActualUsername);
            intent.putExtra("email", userEmail);
            startActivity(intent);
        }
    }

    // --- Implementasi BannerPagerAdapter.OnComicClickListener & AllComicAdapter.OnComicClickListener ---
    // Metode ini akan dipanggil ketika item di banner carousel ATAU rekomendasi diklik
    @Override
    public void onComicClick(ComicResponse.Item comic) {
        // Logika navigasi ke detail komik ketika item banner ATAU item rekomendasi diklik
        // Menggunakan getContext() untuk membuat Intent
        if (getContext() != null) {
            startActivity(createDetailIntent(comic)); // Gunakan method bantu untuk Intent
        } else {
            Log.e("HomeFragment", "Context is null when trying to open detail.");
        }
    }

    // --- Metode bantu untuk membuat Intent ke DetailComicActivity ---
    private Intent createDetailIntent(ComicResponse.Item comic) {
        // Bersinggungan: Menggunakan getContext() untuk membuat Intent
        if (getContext() == null) return null;

        Intent detailIntent = new Intent(getContext(), ComicDetailActivity.class);

        // Memasukkan data komik ke dalam Intent sebagai extra (logika ini tetap sama)
        detailIntent.putExtra("id_comic", comic.getId_comic());
        detailIntent.putExtra("title", comic.getName());
        detailIntent.putExtra("author", comic.getAuthor());
        detailIntent.putExtra("artist", comic.getArtist());
        detailIntent.putExtra("poster", comic.getPoster());
        detailIntent.putExtra("synopsis", comic.getSynopsis());
        detailIntent.putExtra("status", comic.getStatus());
        detailIntent.putExtra("bookmarked",comic.getBookmarked());

        if (comic.getGenre() != null) {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
        } else {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>());
        }

        return detailIntent;
    }
}
