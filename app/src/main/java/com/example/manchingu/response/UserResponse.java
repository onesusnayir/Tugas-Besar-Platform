package com.example.manchingu.response;

public class UserResponse {
    private boolean success;
    private String message;
    private String token;
    private UserData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public UserData getData() {
        return data;
    }

    // Inner class
    public static class UserData {
        private String id_user;
        private String username;
        private String email;
        private String created_at;
        private String updated_at;
        private String deleted_at;

        public String getIdUser() {
            return id_user;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getCreatedAt() {
            return created_at;
        }

        public String getUpdatedAt() {
            return updated_at;
        }

        public String getDeletedAt() {
            return deleted_at;
        }
    }
}
