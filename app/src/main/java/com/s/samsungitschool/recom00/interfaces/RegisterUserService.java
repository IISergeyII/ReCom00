package com.s.samsungitschool.recom00.interfaces;


import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RegisterUserService {
    @POST
    Call<String> register(@Path("login") String login, @Path("email") String email, @Path("password") String password);
}
