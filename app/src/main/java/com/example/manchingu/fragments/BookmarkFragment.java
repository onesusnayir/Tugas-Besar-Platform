package com.example.manchingu.fragments; // Ganti dengan nama package fragment Anda

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // Import ViewGroup
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
// Hapus implementasi BottomNavigationView listener
public class BookmarkFragment extends Fragment
        implements View.OnClickListener {

    private RecyclerView rvBookmark;
    private SharedPreferences prefs;
    private List<BookmarkResponse.Comic> comicList = new ArrayList<>();
    private BookmarkAdapter adapter;
    private ApiService apiService;
    private String token;
    private TextView CompletedBtn, ReadingBtn, DroppedBtn, PlanToReadBtn;
    private BottomNavigationView bottomNav;

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

        ShowBookmarkRecyclerView();

        // Get Token dari SharedPrefereces
        prefs = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token","");
        Log.d("TAG", token);

        // Memanggil fungsi ApiClient.getApiService()
        apiService = ApiClient.getApiService(getActivity());

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

    private void ShowBookmarkRecyclerView() {
        rvBookmark.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new BookmarkAdapter(getActivity(), comicList, new BookmarkAdapter.OnComicClickListener() {
            @Override
            public void onComicClick(BookmarkResponse.Comic comic) {
                Intent intent = new Intent(getActivity(), ComicDetailActivity.class);
                intent.putExtra("title", comic.getName());
                intent.putExtra("author", comic.getAuthor());
                intent.putExtra("artist", comic.getArtist());
                intent.putExtra("poster", comic.getPoster());
                intent.putExtra("synopsis", comic.getSynopsis());
                intent.putExtra("id_comic", comic.getId_comic());
                intent.putStringArrayListExtra("genre", new ArrayList<>(comic.getGenre()));
                startActivity(intent);
            }
        });
        rvBookmark.setAdapter(adapter);
    }

    private void GetBookmark(String status) {
        // memanggil method ApiService.getAllMyBookmark dengan token user
        apiService.getMyBookmark("Bearer "+token, status)
                .enqueue(new Callback<BookmarkResponse>() {
                             @Override
                             public void onResponse(Call<BookmarkResponse> call, Response<BookmarkResponse> response) {
                                 if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                     comicList.clear();
                                     for (BookmarkResponse.Data bookmark : response.body().getData()) {
                                         comicList.add(bookmark.getComic());
                                     }
                                     adapter.notifyDataSetChanged();
                                 }
                             }

                             @Override
                             public void onFailure(Call<BookmarkResponse> call, Throwable t) {

                             }
                         }
                );
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.completed_btn){
            GetBookmark("COMPLETED");
            CompletedBtn.setBackgroundResource(R.drawable.rounded);
            CompletedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            DroppedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        else if (v.getId() == R.id.dropped_btn) {
            GetBookmark("DROPPED");
            DroppedBtn.setBackgroundResource(R.drawable.rounded);
            DroppedBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        else if (v.getId() == R.id.reading_btn) {
            GetBookmark("READING");
            ReadingBtn.setBackgroundResource(R.drawable.rounded);
            ReadingBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            DroppedBtn.setBackground(null);
            PlanToReadBtn.setBackground(null);
        }
        else if (v.getId() == R.id.plan_to_read_btn) {
            GetBookmark("PLAN_TO_READ");
            PlanToReadBtn.setBackgroundResource(R.drawable.rounded);
            PlanToReadBtn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.backgroundBtn));

            CompletedBtn.setBackground(null);
            DroppedBtn.setBackground(null);
            ReadingBtn.setBackground(null);
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
    }
}
