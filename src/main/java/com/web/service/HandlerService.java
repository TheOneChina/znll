package com.web.service;

import com.tnsoft.web.model.AuthResponse;
import com.tnsoft.web.model.UploadResponse;

import java.util.Date;

public interface HandlerService {

    void handleData(String tagNo, Float temperature, Float humidity, Date dataTime, Integer Vdd, Integer
            WIFI, Integer FlashErr);

    UploadResponse handleTagOnlineData(String tagNo, String temperature, Float humidity, Integer FlashErr);

    void handleFeedbackData(String tagNo, String ssid, String password, Integer buzzer, Float precision, Float
            hPrecision,
                            Float tmax, Float tmin, Float hmax, Float hmin);

    AuthResponse handleActiveData(String tagNo, Integer nonce);

}
