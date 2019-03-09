package com.tnsoft.web.model;

import com.tnsoft.hibernate.model.Tag;

import java.io.Serializable;
import java.util.List;

public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String OK = "0";
    public static final String ERROR = "1";

    private String message;
    private String code;

    private List<String> time;
    private List<String> temperature;
    private List<String> humidity;

    private String begin;
    private String end;
    private List<TempItem> temps;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    private List<Tag> tags;

    public Result() {
    }

    public Result(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }

    public List<String> getTemperature() {
        return temperature;
    }

    public void setTemperature(List<String> temperature) {
        this.temperature = temperature;
    }

    public List<String> getHumidity() {
        return humidity;
    }

    public void setHumidity(List<String> humidity) {
        this.humidity = humidity;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getBegin() {
        return begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEnd() {
        return end;
    }

    public void setTemps(List<TempItem> temps) {
        this.temps = temps;
    }

    public List<TempItem> getTemps() {
        return temps;
    }
}
