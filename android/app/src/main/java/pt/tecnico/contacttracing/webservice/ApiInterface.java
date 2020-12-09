package pt.tecnico.contacttracing.webservice;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import pt.tecnico.contacttracing.model.NumberKey;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {
    @GET("/")
    Call<String> getHello();

    @GET("/getinfected")
    Call<Object> getInfected();

    @POST("/sendinfected")
    Call<Void> sendInfected(@Body Object body);

    @POST("/getsignature")
    Call<Object> getSignature(@Body Object body);
}
