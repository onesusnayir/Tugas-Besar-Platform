package com.example.manchingu.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.manchingu.R;
import com.example.manchingu.fragments.BookmarkFragment;
import com.example.manchingu.fragments.HomeFragment;
import com.example.manchingu.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DashboardActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    // Bottom Navbar
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_dashboard);

        // Sesuaikan padding untuk Edge-to-Edge jika diaktifkan
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bottom Navigation Bar
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Muat fragment Home
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            // Set item Home terpilih di nav
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // Method untuk memuat Fragment ke dalam FrameLayout
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
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
            // Muat fragment yang dipilih
            loadFragment(selectedFragment);
            return true; // Menandakan item click ditangani
        }

        return false; // Menandakan item click tidak ditangani
    }
}
