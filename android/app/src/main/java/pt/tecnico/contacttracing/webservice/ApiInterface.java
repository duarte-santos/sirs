package pt.tecnico.contacttracing.webservice;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface ApiInterface {
    @GET("/")
    Call<String> getHello();

    @POST("/getinfected")
    Call<Object> getInfected(@Body Object body);

    @POST("/sendinfected")
    Call<Void> sendInfected(@Body Object body);

    @POST("/getsignature")
    Call<String> getSignature(@Body String body);
}
