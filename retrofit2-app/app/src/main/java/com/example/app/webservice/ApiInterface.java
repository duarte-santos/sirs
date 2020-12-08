package com.example.app.webservice;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("/")
    Call<String> getHello();
}
