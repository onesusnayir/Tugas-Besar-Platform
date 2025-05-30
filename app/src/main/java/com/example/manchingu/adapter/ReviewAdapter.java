package com.example.manchingu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manchingu.R;
import com.example.manchingu.api.ApiClient;
import com.example.manchingu.api.ApiService;
import com.example.manchingu.response.ComicResponse;
import com.example.manchingu.response.ProfileResponse;
import com.example.manchingu.response.ReviewResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    private List<ReviewResponse.ReviewData> reviewList;
    private Context context;
    private ApiService apiService;

    public ReviewAdapter(
            List<ReviewResponse.ReviewData> reviewList,
            Context context
    ){
        this.reviewList = reviewList;
        this.context = context;
    }
    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        apiService = ApiClient.getApiService(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rating_review, parent, false);
        return new ReviewAdapter.ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        ReviewResponse.ReviewData review = reviewList.get(position);


        holder.tvReview.setText(review.getReview_text());
        holder.tvRating.setText(String.valueOf((int) review.getRating()));
        holder.tvTime.setText(review.getCreated_at().split("T")[0]);

        // Set placeholder saat username belum dimuat
        holder.tvUsername.setText("Loading...");
        getReviewUser(review.getId_user(), holder);
    }

    private void getReviewUser(String idUser, ReviewViewHolder holder) {
        apiService.getUserReview(idUser)
                .enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            String username = response.body().getData().getUsername();
                            holder.tvUsername.setText(username);
                        } else {
                            holder.tvUsername.setText("Unknown");
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        holder.tvUsername.setText("Error");
                    }
                });
    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvReview, tvRating, tvTime;
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvReview = itemView.findViewById(R.id.tv_review);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
