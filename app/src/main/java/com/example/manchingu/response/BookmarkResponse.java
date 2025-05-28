package com.example.manchingu.response;

import java.util.List;

public class BookmarkResponse {
    private boolean success;
    private List<Data> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Data> getData() {
        return data;
    }

    public static class Data {
        private String id_bookmark;
        private String id_user;
        private Comic comic;
        private String status;
        private String created_at;
        private String updated_at;

        public String getId_bookmark() {
            return id_bookmark;
        }

        public String getId_user() {
            return id_user;
        }

        public Comic getComic() {
            return comic;
        }

        public String getStatus() {
            return status;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }
    }

    public static class Comic {
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

        public String getId_comic() {
            return id_comic;
        }

        public String getName() {
            return name;
        }

        public String getSynopsis() {
            return synopsis;
        }

        public String getAuthor() {
            return author;
        }

        public String getArtist() {
            return artist;
        }

        public String getStatus() {
            return status;
        }

        public String getPoster() {
            return poster;
        }

        public List<String> getGenre() {
            return genre;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public double getRating() {
            return rating;
        }

        public int getBookmarked() {
            return bookmarked;
        }
    }
}

