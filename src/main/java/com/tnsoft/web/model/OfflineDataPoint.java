package com.tnsoft.web.model;

import java.util.Date;

/**
 * 离线数据中的一组数据
 *
 * @author z
 */
public class OfflineDataPoint {

    private float temperature;
    private float humidity;
    private Date time;

    public OfflineDataPoint(float temperature, float humidity, Date time) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.time = time;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
