package com.example.app.model;

import com.google.gson.annotations.SerializedName;

public class HelloWorld {
    @SerializedName("id")
    private String id;

    public String getId() {
        return id;
    }
}