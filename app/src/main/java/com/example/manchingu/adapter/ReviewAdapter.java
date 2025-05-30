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
import com.example.manchingu.response.ComicResponse;
import com.example.manchingu.response.ProfileResponse;
import com.example.manchingu.response.ReviewResponse;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    private List<ReviewResponse.ReviewData> reviewList;
//    private List<ProfileResponse.Data> userList;
    private Context context;

    public ReviewAdapter(
            List<ReviewResponse.ReviewData> reviewList,
//            List<ProfileResponse.Data> userList,
            Context context
    ){
        this.reviewList = reviewList;
        this.context = context;
//        this.userList = userList;
    }
    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rating_review, parent, false);
        return new ReviewAdapter.ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        ReviewResponse.ReviewData review = reviewList.get(position);
//        ProfileResponse.Data user = userList.get(position);

//        holder.tvUsername.setText(user.getUsername());
        holder.tvReview.setText(review.getReview_text());
        holder.tvRating.setText(String.valueOf((int) review.getRating()));
        holder.tvTime.setText(review.getCreated_at().split("T")[0]);
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
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
