package com.example.manchingu.api;

import android.content.Context;

public class ApiClient {
    private static ApiService apiService;

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = RetrofitBuilder
                    .builder(context.getApplicationContext())
                    .create(ApiService.class);
        }
        return apiService;
    }
}
