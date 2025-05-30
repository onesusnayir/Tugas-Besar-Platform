package com.example.manchingu.response;

import java.util.List;

public class ReviewResponse {
    private boolean success;
    private List<ReviewData> data;

    // Getter & Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ReviewData> getData() {
        return data;
    }

    public void setData(List<ReviewData> data) {
        this.data = data;
    }

    // Nested class for ReviewData
    public static class ReviewData {
        private String id_review;
        private String id_user;
        private String id_comic;
        private int rating;
        private String review_text;
        private String created_at;
        private String updated_at;
        private String deleted_at;
        private Comic comic;

        // Getter & Setter
        public String getId_review() {
            return id_review;
        }

        public void setId_review(String id_review) {
            this.id_review = id_review;
        }

        public String getId_user() {
            return id_user;
        }

        public void setId_user(String id_user) {
            this.id_user = id_user;
        }

        public String getId_comic() {
            return id_comic;
        }

        public void setId_comic(String id_comic) {
            this.id_comic = id_comic;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public String getReview_text() {
            return review_text;
        }

        public void setReview_text(String review_text) {
            this.review_text = review_text;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        public String getDeleted_at() {
            return deleted_at;
        }

        public void setDeleted_at(String deleted_at) {
            this.deleted_at = deleted_at;
        }

        public Comic getComic() {
            return comic;
        }

        public void setComic(Comic comic) {
            this.comic = comic;
        }
    }

    // Nested class for Comic
    public static class Comic {
        private String name;

        // Getter & Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

