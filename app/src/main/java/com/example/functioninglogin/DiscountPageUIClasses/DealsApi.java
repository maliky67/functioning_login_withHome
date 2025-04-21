package com.example.functioninglogin.DiscountPageUIClasses;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DealsApi {

    @GET("deals-v2")
    Call<DealResponse> getDeals(
            @Query("country") String country,
            @Query("price_range") String priceRange,
            @Query("discount_range") String discountRange,
            @Query("num_pages") int numPages
    );
}
