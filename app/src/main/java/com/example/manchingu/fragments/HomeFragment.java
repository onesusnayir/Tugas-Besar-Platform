package com.example.manchingu.fragments; // Ganti dengan nama package fragment Anda

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
import com.example.manchingu.adapter.AllComicAdapter; // Adapter untuk rekomendasi
import com.example.manchingu.adapter.BannerPagerAdapter; // Adapter untuk banner carousel
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse; // Model data dari API
import com.example.manchingu.activities.AllComicsActivity; // Import Activity tujuan
import com.example.manchingu.activities.ComicDetailActivity; // Import Activity tujuan


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implementasikan View.OnClickListener, BannerPagerAdapter.OnComicClickListener, dan AllComicAdapter.OnComicClickListener
// Hapus implementasi BottomNavigationView listener
public class HomeFragment extends Fragment
        implements View.OnClickListener,
        BannerPagerAdapter.OnComicClickListener,
        AllComicAdapter.OnComicClickListener {

    SharedPreferences prefs;
    TextView tvUsername;
    Button seeComicsBtn;
    // BottomNavigationView bottomNav; // Hapus BottomNavigationView

    // --- Untuk Banner Carousel ---
    private ViewPager2 bannerViewPager;
    private BannerPagerAdapter bannerAdapter;
    private Handler sliderHandler; // Inisialisasi di onViewCreated
    private Runnable sliderRunnable; // Inisialisasi di onViewCreated
    private final long SLIDER_DELAY = 3000; // Delay dalam milidetik (3 detik) antar slide

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
        // Inflate the layout for this fragment
        // Cari Views di dalam 'view' yang diinflate
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Inisialisasi Views ---
        // Gunakan getContext() atau getActivity() untuk SharedPreferences
        // Bersinggungan: Akses SharedPreferences dari Fragment menggunakan getContext()
        prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", "defaultName");
        tvUsername = view.findViewById(R.id.username);
        tvUsername.setText(username);

        seeComicsBtn = view.findViewById(R.id.see_all_btn);
        seeComicsBtn.setOnClickListener(this); // Set listener klik untuk tombol Lihat Semua

        // Hapus inisialisasi dan listener untuk bottomNav di sini

        // --- Inisialisasi API Service ---
        // Bersinggungan: Gunakan getContext() untuk ApiClient
        apiService = ApiClient.getApiService(getContext());

        // --- Setup Banner ViewPager2 ---
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        // Bersinggungan: Gunakan getContext() sebagai context untuk adapter
        // Set listener untuk adapter banner ke Fragment ini
        bannerAdapter = new BannerPagerAdapter(this);
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup Auto-Scrolling Banner Runnable (disederhanakan untuk infinite loop)
        // Inisialisasi handler dan runnable di sini atau di onViewCreated
        // Bersinggungan: Handler dan Runnable perlu diinisialisasi di lifecycle Fragment (onCreateView/onViewCreated)
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
                        // Bersinggungan: Menghentikan Handler pada interaksi user
                        sliderHandler.removeCallbacks(sliderRunnable);
                    } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        if (bannerAdapter != null && bannerAdapter.getActualItemCount() > 1) {
                            // Bersinggungan: Memulai kembali Handler
                            sliderHandler.removeCallbacks(sliderRunnable);
                            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                        }
                    }
                }
            }
        });

        // --- Setup RecyclerView Rekomendasi ---
        rvRekomendasi = view.findViewById(R.id.rvRekomendasi);
        // Bersinggungan: Gunakan getContext() sebagai context untuk adapter
        adapter = new AllComicAdapter(getContext(), fetchedComicItems, this); // Pass 'this' karena fragment mengimplement OnComicClickListener

        // Bersinggungan: Gunakan getContext() untuk LayoutManager
        rvRekomendasi.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRekomendasi.setAdapter(adapter);

        // --- Loading ---
        progressBarBanner = view.findViewById(R.id.progressBarBanner);
        progressBarComics = view.findViewById(R.id.progressBarComics);
        progressBarBanner.setVisibility(View.VISIBLE);
        progressBarBanner.setVisibility(View.VISIBLE);

        // --- Panggil API untuk mendapatkan data ---
        fetchComicsData(); // Panggil method untuk fetch data dari API

        return view; // Mengembalikan root view dari fragment layout
    }

    // Pindahkan logika Handler start/stop ke lifecycle Fragment
    // Bersinggungan: Mengelola Handler di onResume() dan onPause() Fragment
    @Override
    public void onResume() {
        super.onResume();
        // Mulai auto-scroll saat fragment aktif
        if (sliderHandler != null && sliderRunnable != null && bannerAdapter != null && bannerAdapter.getActualItemCount() > 1 && bannerViewPager != null && bannerViewPager.getVisibility() == View.VISIBLE) {
            sliderHandler.removeCallbacks(sliderRunnable);
            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
        }
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
        // Pastikan runnable dihapus saat view dihancurkan untuk mencegah memory leaks
        // Bersinggungan: Membersihkan Handler di onDestroyView() Fragment
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
        // Opsional: Null-kan view references untuk mencegah memory leaks (penting pada Fragment)
        tvUsername = null;
        seeComicsBtn = null;
        bannerViewPager = null;
        rvRekomendasi = null;
        adapter = null;
        bannerAdapter = null;
        apiService = null;
        prefs = null;
        // Tidak perlu menull-kan sliderHandler dan sliderRunnable karena sudah di-removeCallbacks
    }


    // Method untuk memanggil API (Logika ini tetap sama, hanya konteksnya berubah)
    private void fetchComicsData() {
        // Bersinggungan: Panggilan API sama, tetapi perlu null check pada apiService dan getContext()
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
        // TODO: Tampilkan loading indicator jika perlu
        // showLoadingIndicator();

        // Panggil API getLimitedComics sesuai kebutuhan Anda (page 1, limit 10 seperti kode awal)
        apiService.getLimitedComics(1, 10).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // Pastikan Fragment masih attached sebelum update UI
                // Bersinggungan: Tambahkan isAdded() check sebelum mengakses context atau update UI
                if (!isAdded() || getContext() == null) {
                    return;
                }

                // TODO: Sembunyikan loading indicator
                progressBarBanner.setVisibility(View.GONE);
                progressBarComics.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                    List<ComicResponse.Item> items = response.body().getData().getItems();

                    if (items != null && !items.isEmpty()) {
                        // Update data di list yang digunakan oleh adapter rekomendasi
                        fetchedComicItems.clear();
                        fetchedComicItems.addAll(items);
                        if (adapter != null) adapter.notifyDataSetChanged(); // Bersinggungan: Pastikan adapter tidak null
                        else Log.w("HomeFragment", "Adapter Rekomendasi is null");


                        // Update data di adapter banner dengan data yang sama
                        if (bannerAdapter != null) bannerAdapter.updateData(items); // Bersinggungan: Pastikan adapter tidak null
                        else Log.w("HomeFragment", "Banner Adapter is null");


                        // Jika ada lebih dari 1 item untuk carousel:
                        if (bannerAdapter != null && bannerAdapter.getActualItemCount() > 1) {
                            // Atur ViewPager2 ke posisi awal yang 'acak' di tengah Integer.MAX_VALUE
                            int initialPosition = Integer.MAX_VALUE / 2;
                            initialPosition = initialPosition - (initialPosition % bannerAdapter.getActualItemCount());

                            if (bannerViewPager != null) { // Bersinggungan: Pastikan ViewPager tidak null
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
                        // Bersinggungan: Gunakan getContext() untuk Toast
                        Toast.makeText(getContext(), "Tidak ada data komik yang ditemukan.", Toast.LENGTH_SHORT).show();
                        // Kosongkan kedua adapter jika tidak ada data
                        fetchedComicItems.clear();
                        if (adapter != null) adapter.notifyDataSetChanged(); // Bersinggungan: Pastikan adapter tidak null
                        if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>()); // Bersinggungan: Pastikan adapter tidak null
                        // Hentikan auto-scroll dan sembunyikan banner
                        if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                        if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Bersinggungan: Pastikan ViewPager tidak null
                    }

                } else {
                    // Handle respon gagal (misalnya, status code 404, 500, dll.)
                    // Bersinggungan: Gunakan getContext() untuk Toast
                    Toast.makeText(getContext(), "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Kosongkan kedua adapter jika gagal
                    fetchedComicItems.clear();
                    if (adapter != null) adapter.notifyDataSetChanged(); // Bersinggungan: Pastikan adapter tidak null
                    if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>()); // Bersinggungan: Pastikan adapter tidak null
                    // Hentikan auto-scroll dan sembunyikan banner
                    if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                    if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Bersinggungan: Pastikan ViewPager tidak null
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // Pastikan Fragment masih attached
                // Bersinggungan: Tambahkan isAdded() check sebelum mengakses context atau update UI
                if (!isAdded() || getContext() == null) {
                    return;
                }

                // TODO: Sembunyikan loading indicator
                // hideLoadingIndicator();
                // Bersinggungan: Gunakan getContext() untuk Toast
                Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace(); // Log error untuk debugging
                // Kosongkan kedua adapter jika error
                fetchedComicItems.clear();
                if (adapter != null) adapter.notifyDataSetChanged(); // Bersinggungan: Pastikan adapter tidak null
                if (bannerAdapter != null) bannerAdapter.updateData(new ArrayList<>()); // Bersinggungan: Pastikan adapter tidak null
                // Hentikan auto-scroll dan sembunyikan banner
                if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
                if (bannerViewPager != null) bannerViewPager.setVisibility(View.GONE); // Bersinggungan: Pastikan ViewPager tidak null
            }
        });
    }


    // --- Implementasi View.OnClickListener (untuk tombol Lihat Semua) ---
    @Override
    public void onClick(View v) {
        // Bersinggungan: Menggunakan getContext() untuk membuat Intent
        if(v.getId() == R.id.see_all_btn && getContext() != null){
            Intent intent = new Intent(getContext(), AllComicsActivity.class);
            startActivity(intent);
            // Hapus finish() karena ini Fragment, bukan Activity yang akan ditutup
            // Optional: finish();
        }
    }

    // --- Implementasi BannerPagerAdapter.OnComicClickListener & AllComicAdapter.OnComicClickListener ---
    // Metode ini akan dipanggil ketika item di banner carousel ATAU rekomendasi diklik
    @Override
    public void onComicClick(ComicResponse.Item comic) {
        // Logika navigasi ke detail komik ketika item banner ATAU item rekomendasi diklik
        // Bersinggungan: Menggunakan getContext() untuk membuat Intent
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

        if (comic.getGenre() != null) {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
        } else {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>());
        }

        return detailIntent;
    }

    // TODO: Implementasikan metode untuk menampilkan/menyembunyikan loading indicator jika Anda punya UI-nya
    // private void showLoadingIndicator() { ... }
    // private void hideLoadingIndicator() { ... }
}
