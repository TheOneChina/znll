package com.tnsoft.web.model;

import java.io.Serializable;
import java.util.List;

public class LocationWithExpressNoList implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private double lat;
    private double lng;
    private List<String> expressNoList;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public List<String> getExpressNoList() {
        return expressNoList;
    }

    public void setExpressNoList(List<String> expressNoList) {
        this.expressNoList = expressNoList;
    }
}
