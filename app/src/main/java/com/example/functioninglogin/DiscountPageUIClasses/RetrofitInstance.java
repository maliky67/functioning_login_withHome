package com.example.functioninglogin.DiscountPageUIClasses;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RetrofitInstance {

    private static final String BASE_URL = "https://real-time-amazon-data.p.rapidapi.com/";

    private static final Retrofit retrofit;

    static {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("x-rapidapi-host", "real-time-amazon-data.p.rapidapi.com")
                                .header("x-rapidapi-key", "e158c548c3msheb43562695abde8p132483jsnc2bc35c17259")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static DealsApi getApi() {
        return retrofit.create(DealsApi.class);
    }
}
