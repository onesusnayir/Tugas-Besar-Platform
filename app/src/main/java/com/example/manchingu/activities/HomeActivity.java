package com.example.manchingu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import android.os.Looper; // Import Looper
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

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
public class HomeActivity extends AppCompatActivity
        implements View.OnClickListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        BannerPagerAdapter.OnComicClickListener { // Tambahkan implementasi listener untuk banner

    SharedPreferences prefs;
    TextView tvUsername;
    Button seeComicsBtn;
    BottomNavigationView bottomNav;

    // --- Untuk Banner Carousel ---
    private ViewPager2 bannerViewPager; // Deklarasi ViewPager2
    private BannerPagerAdapter bannerAdapter; // Deklarasi adapter banner
    private Handler sliderHandler = new Handler(Looper.getMainLooper()); // Handler untuk auto-scroll
    private Runnable sliderRunnable; // Runnable untuk auto-scroll task
    private final long SLIDER_DELAY = 3000; // Delay dalam milidetik (3 detik) antar slide

    // --- Untuk RecyclerView Rekomendasi ---
    RecyclerView rvRekomendasi;
    AllComicAdapter adapter; // Adapter untuk rekomendasi
    // Menggunakan satu list data untuk keduanya
    List<ComicResponse.Item> comicList = new ArrayList<>();

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

        seeComicsBtn = findViewById(R.id.see_all_btn);
        seeComicsBtn.setOnClickListener(this); // Set listener klik untuk tombol Lihat Semua

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home); // Set item Home terpilih secara default
        bottomNav.setOnNavigationItemSelectedListener(this); // Set listener untuk Bottom Navigation

        // --- Inisialisasi API Service ---
        apiService = ApiClient.getApiService(this);


        // --- Setup Banner ViewPager2 ---
        bannerViewPager = findViewById(R.id.bannerViewPager); // Inisialisasi ViewPager2
        // Inisialisasi adapter banner, pass 'this' karena activity mengimplement listenernya
        bannerAdapter = new BannerPagerAdapter(this);
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup Auto-Scrolling Banner
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                int nextItem = currentItem + 1;
                // Loop kembali ke item pertama jika sudah di akhir
                if (nextItem >= bannerAdapter.getItemCount()) {
                    nextItem = 0;
                }
                // Pindah ke item selanjutnya dengan animasi smooth
                bannerViewPager.setCurrentItem(nextItem, true);

                // Jadwalkan runnable ini untuk dijalankan lagi setelah delay
                sliderHandler.postDelayed(this, SLIDER_DELAY);
            }
        };

        // Hentikan auto-scroll saat user berinteraksi, lanjutkan saat berhenti
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // User mulai menyeret, hentikan auto-scroll
                    sliderHandler.removeCallbacks(sliderRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // User berhenti menyeret atau animasi settle selesai, mulai lagi auto-scroll
                    // Beri delay sebelum mulai lagi
                    sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                }
            }
        });


        // --- Setup RecyclerView Rekomendasi ---
        rvRekomendasi = findViewById(R.id.rvRekomendasi); // Inisialisasi RecyclerView
        // Inisialisasi adapter rekomendasi, pass 'this' karena activity mengimplement listenernya
        adapter = new AllComicAdapter(this, comicList, comic -> {
            // TODO: Intent ke detail activity jika ingin (Listener ini sudah ada di kode Anda)
            // Logika di sini sama dengan onComicClick di bawah, jadi bisa dihapus duplikasinya
            // atau pastikan hanya satu tempat yang menangani Intent ke Detail
            // startActivity(createDetailIntent(comic)); // Panggil method bantu jika ingin
        });
        rvRekomendasi.setLayoutManager(new GridLayoutManager(this, 2)); // Set LayoutManager untuk grid
        rvRekomendasi.setAdapter(adapter);
        // Penting: Disable nested scrolling jika di dalam NestedScrollView untuk performa lebih baik
        rvRekomendasi.setNestedScrollingEnabled(false);


        // --- Panggil API untuk mendapatkan data (digunakan untuk banner dan rekomendasi) ---
        fetchComicsData(); // Panggil method untuk fetch data dari API
    }

    // Method untuk memanggil API
    private void fetchComicsData() {
        // TODO: Tampilkan loading indicator jika perlu

        // Panggil API getLimitedComics sesuai kebutuhan Anda (page 1, limit 10 seperti kode awal)
        apiService.getLimitedComics(1, 10).enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(@NonNull Call<ComicResponse> call, @NonNull Response<ComicResponse> response) {
                // TODO: Sembunyikan loading indicator

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<ComicResponse.Item> items = response.body().getData().getItems();

                    if (items != null && !items.isEmpty()) {
                        // Update data di comicList yang digunakan oleh adapter rekomendasi
                        comicList.clear();
                        comicList.addAll(items);
                        adapter.notifyDataSetChanged(); // Beri tahu adapter rekomendasi data berubah

                        // Update data di adapter banner dengan data yang sama
                        bannerAdapter.updateData(items);

                        // Mulai auto-scroll hanya jika ada lebih dari 1 item
                        if (items.size() > 1) {
                            // Beri sedikit delay sebelum slide pertama dimulai
                            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                        }

                    } else {
                        // Handle jika list kosong tapi response sukses
                        Toast.makeText(HomeActivity.this, "Tidak ada data komik yang ditemukan.", Toast.LENGTH_SHORT).show();
                        // Kosongkan kedua adapter jika tidak ada data
                        comicList.clear();
                        adapter.notifyDataSetChanged();
                        bannerAdapter.updateData(new ArrayList<>());
                        // Hentikan auto-scroll jika tidak ada data
                        sliderHandler.removeCallbacks(sliderRunnable);
                    }

                } else {
                    // Handle respon gagal (misalnya, status code 404, 500, dll.)
                    Toast.makeText(HomeActivity.this, "Gagal mendapatkan data: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Kosongkan kedua adapter jika gagal
                    comicList.clear();
                    adapter.notifyDataSetChanged();
                    bannerAdapter.updateData(new ArrayList<>());
                    // Hentikan auto-scroll jika gagal
                    sliderHandler.removeCallbacks(sliderRunnable);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ComicResponse> call, @NonNull Throwable t) {
                // TODO: Sembunyikan loading indicator
                Toast.makeText(HomeActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace(); // Log error untuk debugging
                // Kosongkan kedua adapter jika error
                comicList.clear();
                adapter.notifyDataSetChanged();
                bannerAdapter.updateData(new ArrayList<>());
                // Hentikan auto-scroll jika error
                sliderHandler.removeCallbacks(sliderRunnable);
            }
        });
    }


    // --- Logika Auto-Scrolling Lifecycle ---
    // Dipanggil saat activity kembali ke foreground atau saat pertama kali muncul
    @Override
    protected void onResume() {
        super.onResume();
        // Mulai auto-scroll saat activity aktif, hanya jika ada data dan lebih dari 1 item
        // Logika postDelayed awal dipindahkan ke onResponse setelah data dimuat.
        // Tapi tetap perlu dipanggil di onResume jika activity di-pause lalu dilanjutkan,
        // asalkan data sudah ada dan lebih dari 1 item.
        if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
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
            // NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view_id_anda); // Ganti dengan ID NestedScrollView jika ada
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
        // Logika navigasi ke detail komik ketika item banner diklik
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
}
