package com.example.geofencesystem;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface GeofenceService {
    @POST("api/geofence")
    Call<List<Geofence>> getGeofence(@Body InputData body);
}