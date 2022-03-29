package com.example.ridesharecapstone.api.retrofit;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
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
    Converter<ResponseBody, Integer> INTEGER_CONVERTER =  value -> Integer.valueOf(value.string());
    Converter.Factory INTEGER_FACTORY = new Converter.Factory() {
        @Nullable
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return INTEGER_CONVERTER;
        }

        @Nullable
        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
            return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
        }

        @Nullable
        @Override
        public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return super.stringConverter(type, annotations, retrofit);
        }
    };

    // Add api queries here
    @POST("/api/roles/add/byEmail")
    Call<Void> addRole(@Query("email") String email, @Query("role") String role);

    @GET("/api/user/byEmail")
    Call<JsonObject> getUser(@Query("email") String email);

    @POST("/api/ride/addr/add")
    Call<Integer> addAddr(@Query("line1") String line1, @Query("line2") String line2,
                             @Query("city") String city, @Query("state") String state, @Query("zip") String zip);
    @DELETE("/api/ride/addr/del")
    Call<Void> delAddr(@Query("id") int id);

    @PUT("/api/user/put")
    Call<Void> putUser(@Body RequestBody body);

    @POST("/api/ride/route/add")
    Call<Integer> addRoute(@Query("from") int fromAddressID, @Query("to") int toAddressID,
                              @Query("driver") int driverID);

    @POST("/api/ride/add")
    Call<Integer> addRide(@Query("route") int routeID, @Query("rider") int rider,
                             @Query("datetime") Date datetime);

    // | Simplify Requests
    // v

    Gson GSON = new Gson();
    Retrofit RETROFIT = new Retrofit.Builder()
            /////////////////Link goes here ////////////////////////////
            .baseUrl("https://b1bb-146-168-217-8.ngrok.io")
            //////////////MUST BE HTTPS OR GOOGLE BLOCKS FOR SECURITY/////
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(INTEGER_FACTORY)
            .build();
    Api API = RETROFIT.create(Api.class);

    class ApiResponse<T> {
        private Integer code;
        private T body;
        private Throwable err;

        public boolean hasError() {
            return err != null;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public void setBody(T body) {
            this.body = body;
        }

        public void setErr(Throwable err) {
            this.err = err;
        }

        public Integer getCode() {
            return code;
        }

        public T getBody() {
            return body;
        }

        public Throwable getErr() {
            return err;
        }

        @Override
        public String toString() {
            return "ApiResponse{" +
                    "code=" + code +
                    ", body=" + body +
                    ", err=" + err +
                    '}';
        }
    }

    static <T> T fromJson(JsonObject body, Class<T> type) {
        return GSON.fromJson(GSON.toJson(body), type);
    }

    static <T> ApiResponse<T> handle(Call<T> call, Class<T> type) {
        final ApiResponse<T> apiResponse = new ApiResponse<>();
        final AtomicBoolean ready = new AtomicBoolean(false);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                System.out.println("pass: " + response.code());
                apiResponse.setCode(response.code());
                if (response.body() != null)
                    apiResponse.setBody(response.body());
                apiResponse.setErr(null);
                ready.set(true);
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                System.out.println("fail");
                apiResponse.setCode(null);
                apiResponse.setBody(null);
                apiResponse.setErr(throwable);
                ready.set(true);
                try {
                    throw(throwable);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        final long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start < 10000 && !ready.get()) {
            try { Thread.sleep(50); }
            catch (InterruptedException ignored) { }
            if (ready.get()) break;
        }
        return apiResponse;
    }
}
