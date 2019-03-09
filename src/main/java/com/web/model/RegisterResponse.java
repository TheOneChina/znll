package com.web.model;

import java.io.Serializable;

public class RegisterResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private String device;
    private String key;
    private String token;

    public RegisterResponse() {
        super();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

}
