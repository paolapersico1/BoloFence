package com.example.geofencesystem;

/**
 * Created by Paola on 18/12/2019.
 */
public class Geofence {
    private String message;
    private int pathId;
    private double sleepTime;  //in seconds

    public String getMessage(){
        return this.message;
    }

    public int getPathID(){
        return this.pathId;
    }

    public double getSleepTime() { return this.sleepTime; }

    public void setMessage(String message){
        this.message = message;
    }

    public void setPathId(int pathId){
        this.pathId = pathId;
    }

    public void setSleepTime(double sleepTime) { this.sleepTime = sleepTime; }
}