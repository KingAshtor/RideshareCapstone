package com.example.ridesharecapstone.api.retrofit;


import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Date;
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
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface Api {
    // Add api queries here
    @POST("/api/roles/add/byEmail")
    Call<JsonObject> addRole(@Query("email") String email, @Query("role") String role);

    @GET("/api/user/byEmail")
    Call<JsonObject> getUser(@Query("email") String email);

    @POST("/api/ride/addr/add")
    Call<JsonObject> addAddr(@Query("line1") String line1, @Query("line2") String line2,
                             @Query("city") String city, @Query("state") String state, @Query("zip") String zip);
    @DELETE("/api/ride/addr/del")
    Call<JsonObject> delAddr(@Query("id") int id);

    @PUT("/api/user/put")
    Call<JsonObject> putUser(@Body String body);

    @POST("/api/ride/route/add")
    Call<JsonObject> addRoute(@Query("from") int fromAddressID, @Query("to") int toAddressID,
    @Query("driver") int driverID);

    @POST("/api/ride/add")
    Call<JsonObject> addRide(@Query("route") int routeID, @Query("rider") int rider,
                              @Query("datetime") Date datetime);

     // | Simplify Requests
     // v

    Gson GSON = new Gson();
    ExecutorService SERVICE = new ThreadPoolExecutor(1, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    Retrofit RETROFIT = new Retrofit.Builder()
            /////////////////Link goes here ////////////////////////////
            .baseUrl("https://7e7c-2601-547-1001-1130-00-a49a.ngrok.io")
            //////////////MUST BE HTTPS OR GOOGLE BLOCKS FOR SECURITY/////
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    Api API = RETROFIT.create(Api.class);
    BiConsumer<Void, Response<JsonObject>> DO_NOTHING = (_1, _2) -> { };
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
