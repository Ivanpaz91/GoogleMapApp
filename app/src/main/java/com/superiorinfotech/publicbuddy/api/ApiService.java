package com.superiorinfotech.publicbuddy.api;

import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

/**
 * Created by alex on 19.01.15.
 */
public interface ApiService {

    @GET("/User/{username}/{password}")
    void login(@Path("username") String username, @Path("password") String password, Callback<JsonObject> callback);

    @GET("/Password/{username}/{email}")
    void restorePassword(@Path("username") String username, @Path("email") String email, Callback<JsonObject> callback);

    @GET("/Username/{firstname}/{lastname}/{email}")
    void restoreUsername(@Path("firstname") String username, @Path("lastname") String lastname, @Path("email") String email, Callback<JsonObject> callback);

    @POST("/RegisterUser")
    void registerUser(@Body JsonObject request, Callback<JsonObject> callback);

    @GET("/ChangePassword/{username}/{oldpassword}/{newpassword}")
    void changePassword(@Path("username") String username, @Path("oldpassword") String oldPassword, @Path("newpassword") String newpassword, Callback<JsonObject> callback);

    @Multipart
    @POST("/MP_ReportIncident")
    void reportIncident(@Part("userName")   String username,
                        @Part("entityID")   String entityId,
                        @Part("categoryID") String category,
                        @PartMap Map<String,String> subcategories,
                        @Part("latitude")   String latitude,
                        @Part("longitude")  String longitude,
                        @Part("comments")   String comments,
                        @PartMap Map<String,TypedFile> files,
                        Callback<JsonObject> callback);

    @GET("/Incidents/{username}")
    void getReportedIncidents(@Path("username") String username, Callback<JsonObject> callback);
}
