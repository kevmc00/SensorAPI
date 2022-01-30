package com.sensorapp.sensorapp;

// Object to hold sensor value information
public class SensorValue {
    private String sensorID;
    private String date;
    private String dataType;
    private int value;

    public SensorValue(String sensorID, String date, String dataType, int value){
        this.sensorID = sensorID;
        this.date = date;
        this.dataType = dataType;
        this.value = value;
    }

    public String getID(){
        return this.sensorID;
    }

    public String getSensorID(){
        return this.sensorID;
    }

    public String getDate(){
        return this.date;
    }

    public String getDataType(){
        return this.dataType;
    }

    public int getValue(){
        return value;
    }
}
