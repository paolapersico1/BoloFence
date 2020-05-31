package com.example.geofencesystem;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
/**
 * this class represents the sent geoJson geometry data to server.
 *
 * */
public class Geometry {
    private String type;
    private List<Double> coordinates = null;

    public Geometry(Double latitude, Double longitude) {
        type = "Point";
        coordinates = new LinkedList();
        coordinates.add(latitude);
        coordinates.add(longitude);

    }

    public Geometry(String type, double lng, double lat) {
        this.type = type;
        coordinates = new LinkedList();
        coordinates.add(lng);
        coordinates.add(lat);

    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }


    public String getGeometryAsString() {
        HashMap map = new LinkedHashMap();
        map.put("type", this.getType());
        map.put("coordinates", this.getCoordinates());
        JSONObject json = new JSONObject(map);
        return json.toString();

    }

    public HashMap getGeometryAsMap() {
        HashMap map = new LinkedHashMap();
        map.put("type", this.getType());
        map.put("coordinates", this.getCoordinates());
        return map;

    }
    public JSONObject getGeometryAsJSON() {

        HashMap map = new LinkedHashMap();
        map.put("type", this.getType());
        map.put("coordinates", this.getCoordinates());

        return new JSONObject(map);

    }
}