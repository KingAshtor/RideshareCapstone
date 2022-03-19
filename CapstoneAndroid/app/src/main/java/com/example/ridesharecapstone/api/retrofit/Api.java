package com.example.ridesharecapstone.api.retrofit;


import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
    // Add api queries here
    @GET("/api/user/byEmail")
    Call<JsonObject> getUser(@Query("email") String email);

     // | Simplify Requests
     // v

    Gson GSON = new Gson();
    ExecutorService SERVICE = new ThreadPoolExecutor(1, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl("https://6fb7-146-168-217-8.ngrok.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    Api API = RETROFIT.create(Api.class);
    static <T> void enqueue(BiConsumer<T, Response<JsonObject>> consumer, AppCompatActivity context, Call<JsonObject> call, Class<T> type) {
        SERVICE.submit(() -> call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.code() != 200) return;
                try {
                    consumer.accept(GSON.fromJson(GSON.toJson(response.body()), type), response);
                } catch (JsonSyntaxException ignored) {
                    context.finish();
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) { }
        }));
    }
}