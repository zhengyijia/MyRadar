package com.oneplus.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Oneplus on 2016/10/11.
 */
public class ContactsItem implements Serializable {

    private String name = null;
    private String phoneNumber = null;
    private double Latitude;
    private double Longitude;
    private Date date = null;
    private boolean getLoc = false;

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public double getLatitude(){ return this.Latitude; }

    public void setLatitude(double Latitude){ this.Latitude = Latitude; }

    public double getLongitude(){ return this.Longitude; }

    public void setLongitude(double Longitude){ this.Longitude = Longitude; }

    public Date getDate(){ return this.date; }

    public void setDate(Date date){ this.date = date; }

    public void setGetLoc(boolean getLoc){
        this.getLoc = getLoc;
    }

    public boolean getGetLoc(){
        return this.getLoc;
    }

}

