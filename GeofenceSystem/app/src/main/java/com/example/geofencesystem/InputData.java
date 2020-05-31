package com.example.geofencesystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * this class represents the sent input data to server.
 *
 * */
public class InputData {
    private String type;
    private Geometry geometry;
    private Properties properties;

    /**
     * constructor
     * @param head contains head value of geoJson
     * @param geom contains the geomentry
     * @param pp contains the properties section of geoJson
     * */
    public InputData(String head, Geometry geom, Properties pp) {
        this.type = head;
        this.geometry = geom;
        this.properties = pp;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * it's the core method. it creates the interested GeoJson
     * */
    public JSONObject createGEOJson() {

        JSONObject mj = new JSONObject();
        try {
            mj.put("type", type);
            mj.put("geometry", geometry.getGeometryAsJSON());
            mj.put("properties", properties.getPropertiesAsJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mj;

    }


    public String toString(){
        return this.createGEOJson().toString();
    }


    /*
     * used for debugging
     * */
    public Map<String,String> createMap() {
        Map map = new LinkedHashMap();
        map.put("type", type);
        map.put("geometry", geometry.getGeometryAsMap());
        map.put("properties", properties.getPropertiesAsMap());

        return map;
    }

}
