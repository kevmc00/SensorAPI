package com.sensorapp.sensorapp;

// Object to hold sensor infomation
public class Sensor {
    private String id;
    private String country;
    private String city;

    public Sensor(){}

    public Sensor(String id, String country, String city){
        this.id = id;
        this.country = country;
        this.city = city;
    }

    public String getID(){
        return this.id;
    }

    public String getCountry(){
        return this.country;
    }

    public String getCity(){
        return this.city;
    }
}
