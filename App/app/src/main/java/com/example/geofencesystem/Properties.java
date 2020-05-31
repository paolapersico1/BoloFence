package com.example.geofencesystem;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Properties {

    private String activity;
    private int pathId;
    private String currentGeofence;

    public Properties(String activity, int pathId, String currentGeofence) {
        this.activity = activity;
        this.pathId = pathId;
        this.currentGeofence = currentGeofence;
    }

    public String getActivity() { return activity; }

    public void setActivity(String activity) { this.activity = activity; }

    public int getPathId(){ return pathId; }

    public void setPathId(int pathId){ this.pathId = pathId; }

    public String getCurrentGeofence(){ return currentGeofence; }

    public void setCurrentGeofence(String currentGeofence){ this.currentGeofence = currentGeofence; }


    public String getPropertiesAsString() {
        HashMap map = new LinkedHashMap();
        map.put("activity", getActivity());
        map.put("pathId", getPathId());
        map.put("currentGeofence", getCurrentGeofence());

        JSONObject json = new JSONObject(map);
        return json.toString();

    }

    public HashMap getPropertiesAsMap() {
        HashMap map = new LinkedHashMap();
        map.put("activity", getActivity());
        map.put("pathId", getPathId());
        map.put("currentGeofence", getCurrentGeofence());

        return map;

    }

    public JSONObject getPropertiesAsJSON() {
        HashMap map = new LinkedHashMap();
        map.put("activity", getActivity());
        map.put("pathId", getPathId());
        map.put("currentGeofence", getCurrentGeofence());

        JSONObject json = new JSONObject(map);

        return json;

    }

}