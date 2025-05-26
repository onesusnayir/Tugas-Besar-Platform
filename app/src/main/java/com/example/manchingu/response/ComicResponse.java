package com.example.manchingu.response;

import java.util.List;

public class ComicResponse {
    private boolean success;
    private Data data;

    public ComicResponse() {}

    public ComicResponse(boolean success, Data data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private List<Item> items;
        private int totalCount;

        public Data() {}

        public Data(List<Item> items, int totalCount) {
            this.items = items;
            this.totalCount = totalCount;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }

    public static class Item {
        private String id_comic;
        private String name;
        private String synopsis;
        private String author;
        private String artist;
        private String status;
        private String poster;
        private List<String> genre;
        private String created_at;
        private String updated_at;
        private double rating;
        private int bookmarked;

        public Item() {}

        public Item(
                String id_comic,
                String name,
                String synopsis,
                String author,
                String artist,
                String status,
                String poster,
                List<String> genre,
                String created_at,
                String updated_at,
                double rating,
                int bookmarked
        ) {
            this.id_comic = id_comic;
            this.name = name;
            this.synopsis = synopsis;
            this.author = author;
            this.artist = artist;
            this.status = status;
            this.poster = poster;
            this.genre = genre;
            this.created_at = created_at;
            this.updated_at = updated_at;
            this.rating = rating;
            this.bookmarked = bookmarked;
        }

        public String getId_comic() {
            return id_comic;
        }

        public void setId_comic(String id_comic) {
            this.id_comic = id_comic;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSynopsis() {
            return synopsis;
        }

        public void setSynopsis(String synopsis) {
            this.synopsis = synopsis;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }

        public List<String> getGenre() {
            return genre;
        }

        public void setGenre(List<String> genre) {
            this.genre = genre;
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

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public int getBookmarked() {
            return bookmarked;
        }

        public void setBookmarked(int bookmarked) {
            this.bookmarked = bookmarked;
        }
    }
}
