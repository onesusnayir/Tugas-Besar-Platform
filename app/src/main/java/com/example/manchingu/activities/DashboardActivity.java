package com.example.manchingu.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge; // Jika menggunakan fitur EdgeToEdge
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets; // Jika menggunakan fitur EdgeToEdge
import androidx.core.view.ViewCompat; // Jika menggunakan fitur EdgeToEdge
import androidx.core.view.WindowInsetsCompat; // Jika menggunakan fitur EdgeToEdge
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.manchingu.R;
import com.example.manchingu.fragments.BookmarkFragment;
import com.example.manchingu.fragments.HomeFragment;
import com.example.manchingu.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView; // Gunakan ini

public class DashboardActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Aktifkan Edge-to-Edge jika diinginkan (sesuai template baru)
        EdgeToEdge.enable(this); // Jika menggunakan fitur EdgeToEdge

        setContentView(R.layout.activity_dashboard);

        // Sesuaikan padding untuk Edge-to-Edge jika diaktifkan
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(this); // Set listener ke Activity ini

        // Muat fragment default saat pertama kali Activity dibuat (misal: HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Panggil method bantu untuk memuat fragment
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set item Home terpilih di nav
        }
    }

    // Method bantu untuk memuat Fragment ke dalam FrameLayout
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Ganti fragment_container dengan fragment baru
                .commit(); // Selesaikan transaksi
    }

    // Implementasi listener untuk BottomNavigationView
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_bookmark) {
            selectedFragment = new BookmarkFragment();
        } else if (itemId == R.id.nav_search) {
            selectedFragment = new SearchFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment); // Muat fragment yang dipilih
            return true; // Menandakan item click ditangani
        }

        return false; // Menandakan item click tidak ditangani
    }

    // Optional: Tambahkan onBackPressed jika perlu mengelola back stack Fragment
    // @Override
    // public void onBackPressed() {
    //     FragmentManager fm = getSupportFragmentManager();
    //     if (fm.getBackStackEntryCount() > 0) {
    //         fm.popBackStack();
    //     } else {
    //         super.onBackPressed();
    //     }
    // }
}
