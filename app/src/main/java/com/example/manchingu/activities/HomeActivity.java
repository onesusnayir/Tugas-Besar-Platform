package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import android.os.Looper; // Import Looper
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Import Toast
import android.widget.ImageView; // Import ImageView (jika digunakan untuk profile)


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2; // Import ViewPager2

import com.example.manchingu.R;
import com.example.manchingu.adapter.AllComicAdapter; // Adapter untuk rekomendasi
import com.example.manchingu.adapter.BannerPagerAdapter; // Adapter untuk banner carousel
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse; // Model data dari API
// import com.google.android.material.bottomnavigation.BottomNavigationItemView; // Tidak digunakan
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implementasi listener untuk View.OnClickListener, BottomNavigationView dan BannerPagerAdapter
// Kita juga perlu mengimplementasikan OnComicClickListener dari AllComicAdapter
// agar bisa menangani klik item rekomendasi di dalam Activity ini.
public class HomeActivity extends AppCompatActivity
        implements View.OnClickListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        BannerPagerAdapter.OnComicClickListener, // Listener untuk item banner
        AllComicAdapter.OnComicClickListener { // Tambahkan listener untuk item rekomendasi dari AllComicAdapter

    SharedPreferences prefs;
    TextView tvUsername;
    Button seeComicsBtn;
    BottomNavigationView bottomNav;
    ProgressBar progressBarBanner, progressBarComics;

    // --- Untuk Banner Carousel ---
    private ViewPager2 bannerViewPager; // Deklarasi ViewPager2
    private BannerPagerAdapter bannerAdapter; // Deklarasi adapter banner
    private Handler sliderHandler = new Handler(Looper.getMainLooper()); // Handler untuk auto-scroll
    private Runnable sliderRunnable; // Runnable untuk auto-scroll task
    private final long SLIDER_DELAY = 3000; // Delay dalam milidetik (3 detik) antar slide

    // --- Untuk RecyclerView Rekomendasi ---
    RecyclerView rvRekomendasi;
    AllComicAdapter adapter; // Adapter untuk rekomendasi
    // Menggunakan satu list data untuk keduanya (data yang diambil dari API)
    List<ComicResponse.Item> fetchedComicItems = new ArrayList<>(); // Mengubah nama variabel agar lebih jelas


    ApiService apiService; // Deklarasi ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Menambahkan padding untuk menghindari UI tertutup oleh system bars
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // --- Inisialisasi Views ---
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "defaultName");
        tvUsername = findViewById(R.id.username);
        tvUsername.setText(username);
        progressBarBanner = findViewById(R.id.progressBarBanner);
        progressBarComics = findViewById(R.id.progressBarComics);
        progressBarBanner.setVisibility(View.VISIBLE);
        progressBarComics.setVisibility(View.VISIBLE);

        seeComicsBtn = findViewById(R.id.see_all_btn);
        seeComicsBtn.setOnClickListener(this); // Set listener klik untuk tombol Lihat Semua

        bottomNav = findViewById(R.id.bottomNavigation);
        // Set item Home terpilih secara default, tunda sedikit agar UI rendering selesai
        bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.nav_home));
        bottomNav.setOnNavigationItemSelectedListener(this); // Set listener untuk Bottom Navigation

        // --- Inisialisasi API Service ---
        apiService = ApiClient.getApiService(this);


        // --- Setup Banner ViewPager2 ---
        bannerViewPager = findViewById(R.id.bannerViewPager); // Inisialisasi ViewPager2
        // Inisialisasi adapter banner, pass 'this' karena activity mengimplement listenernya
        bannerAdapter = new BannerPagerAdapter(this);
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup Auto-Scrolling Banner Runnable (disederhanakan untuk infinite loop)
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                // Cukup geser ke item selanjutnya. Adapter yang mengurus loop tak terbatas.
                bannerViewPager.setCurrentItem(bannerViewPager.getCurrentItem() + 1, true);

                // Jadwalkan runnable ini untuk dijalankan lagi setelah delay
                // Tidak perlu cek getActualItemCount() di sini, cukup di onResume & onResponse
                // PENTING: Selalu batalkan callback sebelumnya sebelum menjadwalkan yang baru
                sliderHandler.removeCallbacks(this); // Batalkan runnable ini sendiri
                sliderHandler.postDelayed(this, SLIDER_DELAY);
            }
        };

        // Hentikan auto-scroll saat user berinteraksi, lanjutkan saat berhenti
        // Ini tetap diperlukan untuk mengontrol handler dan memberikan pengalaman yang baik
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // User mulai menyeret, hentikan auto-scroll
                    sliderHandler.removeCallbacks(sliderRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // User berhenti menyeret atau animasi settle selesai, mulai lagi auto-scroll
                    // Pastikan ada lebih dari 1 item sebelum memulai kembali
                    if (bannerAdapter != null && bannerAdapter.getActualItemCount() > 1) {
                        // Batalkan callback apa pun sebelum menjadwalkan
                        sliderHandler.removeCallbacks(sliderRunnable);
                        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                    }
                }
            }
        });


        // --- Setup RecyclerView Rekomendasi ---
        rvRekomendasi = findViewById(R.id.rvRekomendasi); // Inisialisasi RecyclerView
        // Inisialisasi adapter rekomendasi dengan list data yang sama, pass listener
        // Mengubah lambda agar memanggil method onComicClick yang diimplementasikan di activity
        // This approach ensures both banner clicks and rekomendasi clicks are handled centrally
        adapter = new AllComicAdapter(this, fetchedComicItems, this::onComicClick);

        rvRekomendasi.setLayoutManager(new GridLayoutManager(this, 2)); // Set LayoutManager untuk grid
        rvRekomendasi.setAdapter(adapter);
        // Tidak perlu setNestedScrollingEnabled(false) jika RecyclerView adalah elemen scrollable utama (dengan app:layout_behavior)
        // rvRekomendasi.setNestedScrollingEnabled(false); // Hapus baris ini


        // --- Panggil API untuk mendapatkan data (digunakan untuk banner dan rekomendasi) ---
        fetchComicsData(); // Panggil method untuk fetch data dari API
    }

    // Method untuk memanggil API
    private void fetchComicsData() {
        // TODO: Tampilkan loading indicator jika perlu
        // showLoadingIndicator();

        // Panggil API getLimitedComics sesuai kebutuhan Anda (page 1, limit 10 seperti kode awal)
        apiService.getLimitedComics(1, 10).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // TODO: Sembunyikan loading indicator
                // hideLoadingIndicator();

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<ComicResponse.Item> items = response.body().getData().getItems();
                    progressBarBanner.setVisibility(View.GONE);
                    progressBarComics.setVisibility(View.GONE);

                    if (items != null && !items.isEmpty()) {
                        // Update data di list yang digunakan oleh adapter rekomendasi
                        fetchedComicItems.clear();
                        fetchedComicItems.addAll(items);
                        adapter.notifyDataSetChanged(); // Beri tahu adapter rekomendasi data berubah

                        // Update data di adapter banner dengan data yang sama
                        bannerAdapter.updateData(items);

                        // Jika ada lebih dari 1 item untuk carousel:
                        if (bannerAdapter.getActualItemCount() > 1) {
                            // Atur ViewPager2 ke posisi awal yang 'acak' di tengah Integer.MAX_VALUE
                            // untuk memberikan efek loop tak terbatas yang mulus
                            int initialPosition = Integer.MAX_VALUE / 2;
                            // Sesuaikan posisi awal agar merupakan kelipatan dari jumlah item asli
                            // Ini penting agar item yang ditampilkan di awal selalu item data pertama (index 0)
                            initialPosition = initialPosition - (initialPosition % bannerAdapter.getActualItemCount());

                            bannerViewPager.setCurrentItem(initialPosition, false); // Set posisi awal tanpa animasi

                            // Batalkan callback apa pun sebelum menjadwalkan
                            sliderHandler.removeCallbacks(sliderRunnable);
                            // Beri sedikit delay sebelum slide otomatis pertama dimulai
                            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);

                            // Pastikan banner terlihat
                            bannerViewPager.setVisibility(View.VISIBLE);
                        } else {
                            // Jika 0 atau 1 item, hentikan auto-scroll dan sembunyikan banner
                            sliderHandler.removeCallbacks(sliderRunnable);
                            bannerViewPager.setVisibility(View.GONE);
                        }

                    } else {
                        // Handle jika list kosong tapi response sukses
                        Toast.makeText(HomeActivity.this, "Tidak ada data komik yang ditemukan.", Toast.LENGTH_SHORT).show();
                        // Kosongkan kedua adapter jika tidak ada data
                        fetchedComicItems.clear();
                        adapter.notifyDataSetChanged();
                        bannerAdapter.updateData(new ArrayList<>());
                        // Hentikan auto-scroll dan sembunyikan banner
                        sliderHandler.removeCallbacks(sliderRunnable);
                        bannerViewPager.setVisibility(View.GONE);
                    }

                } else {
                    // Handle respon gagal (misalnya, status code 404, 500, dll.)
                    Toast.makeText(HomeActivity.this, "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Kosongkan kedua adapter jika gagal
                    fetchedComicItems.clear();
                    adapter.notifyDataSetChanged();
                    bannerAdapter.updateData(new ArrayList<>());
                    // Hentikan auto-scroll dan sembunyikan banner
                    sliderHandler.removeCallbacks(sliderRunnable);
                    bannerViewPager.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // TODO: Sembunyikan loading indicator
                // hideLoadingIndicator();
                Toast.makeText(HomeActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace(); // Log error untuk debugging
                // Kosongkan kedua adapter jika error
                fetchedComicItems.clear();
                adapter.notifyDataSetChanged();
                bannerAdapter.updateData(new ArrayList<>());
                // Hentikan auto-scroll dan sembunyikan banner
                sliderHandler.removeCallbacks(sliderRunnable);
                bannerViewPager.setVisibility(View.GONE);
            }
        });
    }


    // --- Logika Auto-Scrolling Lifecycle ---
    // Dipanggil saat activity kembali ke foreground atau saat pertama kali muncul
    @Override
    protected void onResume() {
        super.onResume();
        // Mulai auto-scroll saat activity aktif, hanya jika ada data lebih dari 1 item
        // dan ViewPager2 terlihat.
        // Penjadwalan awal dilakukan di fetchComicsData. Ini untuk melanjutkan setelah Pause.
        if (bannerAdapter != null && bannerAdapter.getActualItemCount() > 1 && bannerViewPager.getVisibility() == View.VISIBLE) {
            // Batalkan callback apa pun sebelum menjadwalkan kembali
            sliderHandler.removeCallbacks(sliderRunnable);
            // Jadwalkan runnable untuk dijalankan kembali setelah resume
            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
        }

        // Pastikan item bottom nav yang aktif saat activity muncul
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    // Dipanggil saat activity tidak lagi di foreground (misalnya pindah ke activity lain)
    @Override
    protected void onPause() {
        super.onPause();
        // Hentikan auto-scroll saat activity tidak terlihat
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    // Dipanggil saat activity dihancurkan
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Pastikan runnable dihapus saat activity dihancurkan untuk mencegah memory leaks
        sliderHandler.removeCallbacks(sliderRunnable);
    }


    // --- Implementasi View.OnClickListener (untuk tombol Lihat Semua) ---
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.see_all_btn){
            Intent intent = new Intent(HomeActivity.this, AllComicsActivity.class);
            startActivity(intent);
            // Optional: finish(); // Tutup HomeActivity jika tidak ingin kembali ke sini
        }
    }

    // --- Implementasi BottomNavigationView.OnNavigationItemSelectedListener ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Jika sudah di Home, tidak perlu pindah activity lagi
        if (itemId == R.id.nav_home) {
            // Optional: Scroll ke atas halaman jika di Home
            // Jika Anda menggunakan NestedScrollView di XML, ID-nya harus sesuai
            // NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view_id_anda);
            // if (nestedScrollView != null) {
            //    nestedScrollView.scrollTo(0, 0);
            // }
            return true; // Item klik sudah ditangani
        } else if (itemId == R.id.nav_bookmark) {
            Intent intent = new Intent(HomeActivity.this, BookmarkActivity.class);
            startActivity(intent);
            finish(); // Opsional: tutup activity saat ini agar tidak menumpuk
            return true; // Item klik sudah ditangani
        } else if (itemId == R.id.nav_search) {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
            finish(); // Opsional: tutup activity saat ini agar tidak menumpuk
            return true; // Item klik sudah ditangani
        }

        return false; // Item klik tidak ditangani
    }

    // --- Implementasi BannerPagerAdapter.OnComicClickListener ---
    // Metode ini akan dipanggil ketika item di banner carousel diklik
    @Override
    public void onComicClick(ComicResponse.Item comic) {
        // Logika navigasi ke detail komik ketika item banner ATAU item rekomendasi diklik
        startActivity(createDetailIntent(comic)); // Gunakan method bantu untuk Intent
    }

    // --- Metode bantu untuk membuat Intent ke DetailComicActivity ---
    private Intent createDetailIntent(ComicResponse.Item comic) {
        Intent detailIntent = new Intent(this, ComicDetailActivity.class);

        // Memasukkan data komik ke dalam Intent sebagai extra
        // Pastikan nama key sesuai dengan yang diterima di ComicDetailActivity
        detailIntent.putExtra("id_comic", comic.getId_comic()); // Menggunakan ID unik
        detailIntent.putExtra("title", comic.getName());
        detailIntent.putExtra("author", comic.getAuthor());
        detailIntent.putExtra("artist", comic.getArtist());
        detailIntent.putExtra("poster", comic.getPoster()); // URL gambar poster
        detailIntent.putExtra("synopsis", comic.getSynopsis()); // Sinopsis

        // Pastikan comic.getGenre() tidak null sebelum dimasukkan ke ArrayListExtra
        if (comic.getGenre() != null) {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
        } else {
            detailIntent.putStringArrayListExtra("genre", new ArrayList<>()); // Kirim list kosong jika null
        }

        // Jika ada data lain yang perlu dilewatkan, tambahkan di sini
        // detailIntent.putExtra("rating", comic.getRating());

        return detailIntent;
    }

    // TODO: Implementasikan metode untuk menampilkan/menyembunyikan loading indicator jika Anda punya UI-nya
    // private void showLoadingIndicator() { ... }
    // private void hideLoadingIndicator() { ... }
}
