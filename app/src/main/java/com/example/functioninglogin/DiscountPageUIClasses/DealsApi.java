package com.example.functioninglogin.DiscountPageUIClasses;

import com.example.functioninglogin.DiscountPageUIClasses.DealItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DealsApi {
    @GET("b1a795c2-90e0-4fe7-b054-1f9ddfd56cfc")
    Call<List<DealItem>> getDeals();
}
