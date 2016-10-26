package com.oneplus.model;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polyline;

/**
 * Created by Oneplus on 2016/10/20.
 */
public class MarkerItem {

    private Marker marker = null;
    private Polyline line1 = null;
    private Polyline line2 = null;
    private Marker distanceMarker = null;

    public Marker getMarker(){
        return marker;
    }

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    public Polyline getLine1(){
        return line1;
    }

    public void setLine1(Polyline line1){
        this.line1 = line1;
    }

    public Polyline getLine2(){
        return line2;
    }

    public void setLine2(Polyline line2){
        this.line2 = line2;
    }

    public Marker getDistanceMarker(){ return distanceMarker; }

    public void setDistanceMarker(Marker distanceMarker){
        this.distanceMarker = distanceMarker;
    }

}
