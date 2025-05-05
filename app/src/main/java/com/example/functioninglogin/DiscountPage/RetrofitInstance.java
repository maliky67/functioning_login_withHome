package com.example.functioninglogin.DiscountPage;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private static final String BASE_URL = "https://real-time-amazon-data.p.rapidapi.com/";

    private static final Retrofit retrofit;

    static {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("x-rapidapi-host", "real-time-amazon-data.p.rapidapi.com")
                            .header("x-rapidapi-key", "e158c548c3msheb43562695abde8p132483jsnc2bc35c17259")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
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
