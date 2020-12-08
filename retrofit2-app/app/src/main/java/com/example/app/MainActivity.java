package com.example.app;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.webservice.ApiInterface;
import com.example.app.webservice.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHello();
    }

    private void getHello(){
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<String> call = apiInterface.getHello();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                System.out.println(response);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println("fuck");
            }
        });
    }
}