package com.example.gps_app;

import java.io.Serializable;

public class mCoords implements Serializable {
    private double lat;
    private double lng;

    public mCoords(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
