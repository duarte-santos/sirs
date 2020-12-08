package pt.tecnico.contacttracing.webservice;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("/")
    Call<String> getHello();
}
