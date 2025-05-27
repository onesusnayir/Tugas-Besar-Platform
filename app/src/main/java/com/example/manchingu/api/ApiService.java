package com.example.manchingu.api;

import com.example.manchingu.model.User;
import com.example.manchingu.response.BookmarkResponse;
import com.example.manchingu.response.ComicResponse;
import com.example.manchingu.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/auth/register")
    Call<UserResponse> createUser(@Body User userRequest);

    @POST("/auth/login")
    Call<UserResponse> login(@Body User userRequest);

    @GET("/comic/all")
    Call<ComicResponse> getLimitedComics( @Query("page") int page, @Query("limit") int limit);

    @GET("/comic/all")
    Call<ComicResponse> getAllComics();

    @GET("/bookmark/my")
    Call<BookmarkResponse> getAllMyBookmark(@Header("Authorization") String token);

}
