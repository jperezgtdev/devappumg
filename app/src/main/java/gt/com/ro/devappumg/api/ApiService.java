package gt.com.ro.devappumg.api;

import gt.com.ro.devappumg.models.LoginRequest;
import gt.com.ro.devappumg.models.LoginResponse;
import gt.com.ro.devappumg.models.UserResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/usuarios/{id}")
    Call<UserResponse> getUserById(@Header("Authorization") String token, @Path("id") int id);
}