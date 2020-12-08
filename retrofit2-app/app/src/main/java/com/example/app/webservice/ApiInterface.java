package com.example.app.webservice;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {
    @GET("/")
    Call<String> getHello();

    @POST("/saveinfected")
    Call<Object> saveInfected(@Body Integer body);
}
