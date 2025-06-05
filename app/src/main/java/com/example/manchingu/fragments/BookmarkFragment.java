package com.example.manchingu.fragments; // Ganti dengan nama package fragment Anda

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // Import ViewGroup
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Hapus import BottomNavigationView dan NavigationBarView

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implementasikan View.OnClickListener dan BookmarkAdapter.OnComicClickListener
public class BookmarkFragment extends Fragment
        implements View.OnClickListener {
    // Recycler View Daftar Bookmark
    private RecyclerView rvBookmark;
    private BookmarkAdapter adapter;
    // SharedPreferences untuk Token
    private SharedPreferences prefs;
    // List Comics
    private List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    // Api Client
    private ApiService apiService;
    // Token autentikasi dari SharedPreferences
    private String token;
    // Kategori Bookmark Button
    private TextView CompletedBtn;
    private TextView ReadingBtn;
    private TextView DroppedBtn;
    private TextView PlanToReadBtn;
    //Loading
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        // Inisialisasi
        rvBookmark = view.findViewById(R.id.rvBookmark);
        CompletedBtn = view.findViewById(R.id.completed_btn);
        ReadingBtn = view.findViewById(R.id.reading_btn);
        DroppedBtn = view.findViewById(R.id.dropped_btn);
        PlanToReadBtn = view.findViewById(R.id.plan_to_read_btn);

        // Menampilkan RecyclerView
        ShowBookmarkRecyclerView();

        // Get Token dari SharedPrefereces
        prefs = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token","");
        Log.d("TAG", token);

        // Memanggil fungsi ApiClient.getApiService()
        apiService = ApiClient.getApiService(getActivity());

        // Show Loading dan Matikan Button
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Menampilkan Bookmark Status = Completed
        GetBookmark("COMPLETED");
        CompletedBtn.setBackgroundResource(R.drawable.rounded);
        CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

        // SetOnClickListener
        CompletedBtn.setOnClickListener(this);
        ReadingBtn.setOnClickListener(this);
        DroppedBtn.setOnClickListener(this);
        PlanToReadBtn.setOnClickListener(this);

        return view;
    }

    // Method OnClick
    @Override
    public void onClick(View v) {
        // Menampilkan Daftar Bookmark dengan status = Completed
        if (v.getId() == R.id.completed_btn){
            // Loading
            progressBar.setVisibility(View.VISIBLE);
            // Fetch Bookmark status = completed
            GetBookmark("COMPLETED");
            CompletedBtn.setBackgroundResource(R.drawable.rounded);
            CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            DroppedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        // Menampilkan Daftar Bookmark dengan status = Dropped
        else if (v.getId() == R.id.dropped_btn) {
            // Loading
            progressBar.setVisibility(View.VISIBLE);
            // Fetch Bookmark status = Dropped
            GetBookmark("DROPPED");
            DroppedBtn.setBackgroundResource(R.drawable.rounded);
            DroppedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        // Menampilkan Daftar Bookmark dengan status = Reading
        else if (v.getId() == R.id.reading_btn) {
            // Loading
            progressBar.setVisibility(View.VISIBLE);
            // Fetch Bookmark status = Reading
            GetBookmark("READING");
            ReadingBtn.setBackgroundResource(R.drawable.rounded);
            ReadingBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            DroppedBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        // Menampilkan Daftar Bookmark dengan status = Plan to Read
        else if (v.getId() == R.id.plan_to_read_btn) {
            // Loading
            progressBar.setVisibility(View.VISIBLE);
            // Fetch Bookmark status = Plan to Read
            GetBookmark("PLAN_TO_READ");
            PlanToReadBtn.setBackgroundResource(R.drawable.rounded);
            PlanToReadBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            DroppedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
        }
    }

    // Method Recycler View Bookmark
    private void ShowBookmarkRecyclerView() {
        rvBookmark.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new BookmarkAdapter(getActivity(), comicList, new BookmarkAdapter.OnComicClickListener() {
            // Recycler View OnClick
            @Override
            public void onComicClick(BookmarkResponse.Comic comic) {
                // Pindah Ke Halaman Detail Comic Activity
                Intent intent = new Intent(getActivity(), ComicDetailActivity.class);
                intent.putExtra("title", comic.getName());
                intent.putExtra("author", comic.getAuthor());
                intent.putExtra("artist", comic.getArtist());
                intent.putExtra("poster", comic.getPoster());
                intent.putExtra("synopsis", comic.getSynopsis());
                intent.putExtra("id_comic", comic.getId_comic());
                intent.putExtra("status",comic.getStatus());
                intent.putExtra("bookmarked",comic.getBookmarked());
                intent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
                startActivity(intent);
            }
        });
        rvBookmark.setAdapter(adapter);
    }


    // Fetch Bookmark Berdasarkan Status
    private void GetBookmark(String status) {
        // Memanggil method ApiService.getAllMyBookmark dengan token user
        apiService.getMyBookmark("Bearer "+token, status)
            .enqueue(new Callback<BookmarkResponse>() {
                 @Override
                 public void onResponse(Call<BookmarkResponse> call, Response<BookmarkResponse> response) {
                     if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                         progressBar.setVisibility(View.GONE);
                         comicList.clear();
                         for (BookmarkResponse.Data bookmark : response.body().getData()) {
                             comicList.add(bookmark.getComic());
                         }
                         adapter.notifyDataSetChanged();
                     }
                 }

                 @Override
                 public void onFailure(Call<BookmarkResponse> call, Throwable t) {
                         progressBar.setVisibility(View.GONE);

                 }
             }
        );
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
        progressBar = null;
    }
}
