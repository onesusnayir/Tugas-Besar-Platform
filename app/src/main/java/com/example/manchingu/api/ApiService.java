package com.example.manchingu.api;

import com.example.manchingu.model.User;
import com.example.manchingu.response.BookmarkResponse;
import com.example.manchingu.response.ComicResponse;
import com.example.manchingu.response.ProfileResponse;
import com.example.manchingu.response.ReviewResponse;
import com.example.manchingu.response.UserResponse;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Auth
    @POST("/auth/register")
    Call<UserResponse> createUser(@Body User userRequest);
    @POST("/auth/login")
    Call<UserResponse> login(@Body User userRequest);

    // Comic
    @GET("/comic/all")
    Call<ComicResponse> getLimitedComics(
            @Query("page") int page,
            @Query("limit") int limit
    );
    @GET("/comic/all")
    Call<ComicResponse> getAllComics();
    @GET("/comic/all")
    Call<ComicResponse> getComicByTitle(
            @Query("key") String key
    );

    // Bookmark
    @GET("/bookmark/my")
    Call<BookmarkResponse> getMyBookmark(
            @Header("Authorization") String token,
            @Query("status") String status
    );
    @GET("/bookmark/my")
    Call<BookmarkResponse> getAllMyBookmark(
            @Header("Authorization") String token
    );
    @POST("/bookmark/new/{comickId}")
    Call<JsonObject> insertNewBookmark(
            @Header("Authorization") String token,
            @Path("comickId") String comickId,
            @Query("status") String status
    );
    @DELETE("/bookmark/{bookmarkId}")
    Call<JsonObject> deleteBookmark(
            @Header("Authorization") String token,
            @Path("bookmarkId") String bookmarkId
    );
    @PUT("bookmark/{bookmarkId}")
    Call<JsonObject> updateBookmark(
            @Header("Authorization") String token,
            @Path("bookmarkId") String bookmarkId,
            @Query("status") String newStatus
    );

    // Review
    @POST("/review/new/{comicId}")
    Call<JsonObject> insertReview(
            @Header("Authorization") String token,
            @Path("comicId") String comickId,
            @Body JsonObject reviewBody
    );
    @GET("/review/comic/{comicId}")
    Call<ReviewResponse> getComicReviews(
        @Header("Authorization") String token,
        @Path("comicId") String comickId
    );
    @GET("/review/user")
    Call<ReviewResponse> getUserReview(
        @Header("Authorization") String token,
        @Query("key") String key
    );
    @DELETE("/review/{reviewId}")
    Call<JsonObject> deleteUserReview(
        @Header("Authorization") String token,
        @Path("reviewId") String reviewId
    );
    @PUT("/review/{reviewId}")
    Call<JsonObject> updateUserReview(
        @Header("Authorization") String token,
        @Path("reviewId") String reviewId,
        @Body JsonObject body
    );
    // User
    @GET("/user/{userId}")
    Call<ProfileResponse> getUserReview(
        @Path("userId") String userId
    );
    @GET("user/my")
    Call<ProfileResponse> getMyProfile(
            @Header("Authorization") String token
    );
}
