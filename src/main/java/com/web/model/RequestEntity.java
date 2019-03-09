package com.web.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 硬件发送到云端指令映射实体
 */
public class RequestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;
    private String method;
    private Meta meta;
    private Body body;
    private Integer nonce;
    private Date Time;
    private Integer SleepTime;
    private String ssid;
    private String password;
    private Float temPrecision;
    private Float humPrecision;
    private Float tmax;
    private Float tmin;
    private Float hmax;
    private Float hmin;
    private Integer buzzer;
    private Integer Vdd;//电池电压
    private Integer WIFI; //wifi开关状态
    private Integer FlashErr;


    public RequestEntity() {
        super();
    }

    public Integer getVdd() {
        return Vdd;
    }

    public void setVdd(Integer vdd) {
        Vdd = vdd;
    }

    public Integer getWIFI() {
        return WIFI;
    }

    public void setWIFI(Integer WIFI) {
        this.WIFI = WIFI;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Float getTemPrecision() {
        return temPrecision;
    }

    public void setTemPrecision(Float temPrecision) {
        this.temPrecision = temPrecision;
    }

    public Float getHumPrecision() {
        return humPrecision;
    }

    public void setHumPrecision(Float humPrecision) {
        this.humPrecision = humPrecision;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setMeta(RequestEntity.Meta meta) {
        this.meta = meta;
    }

    public RequestEntity.Meta getMeta() {
        return meta;
    }

    public void setBody(RequestEntity.Body body) {
        this.body = body;
    }

    public RequestEntity.Body getBody() {
        return body;
    }

    public void setTime(Date Time) {
        this.Time = Time;
    }

    public Date getTime() {
        return Time;
    }

    public Integer getSleepTime() {
        return SleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        SleepTime = sleepTime;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Float getTmax() {
        return tmax;
    }

    public void setTmax(Float tmax) {
        this.tmax = tmax;
    }

    public Float getTmin() {
        return tmin;
    }

    public void setTmin(Float tmin) {
        this.tmin = tmin;
    }

    public Float getHmax() {
        return hmax;
    }

    public void setHmax(Float hmax) {
        this.hmax = hmax;
    }

    public Float getHmin() {
        return hmin;
    }

    public void setHmin(Float hmin) {
        this.hmin = hmin;
    }

    public Integer getBuzzer() {
        return buzzer;
    }

    public void setBuzzer(Integer buzzer) {
        this.buzzer = buzzer;
    }

    public Integer getFlashErr() {
        return FlashErr;
    }

    public void setFlashErr(Integer flashErr) {
        FlashErr = flashErr;
    }


    /////////////////////////////////////////////////////////////
    public static class Meta implements Serializable {

        private static final long serialVersionUID = 1L;

        private String Authorization;

        public void setAuthorization(String Authorization) {
            this.Authorization = Authorization;
        }

        public String getAuthorization() {
            return Authorization;
        }
    }

    public static class Body implements Serializable {

        private static final long serialVersionUID = 1L;

        private String encrypt_method;
        private String bssid;
        private String token;
        private Datapoint datapoint;
        private String offlineData;

        public void setEncrypt_method(String encrypt_method) {
            this.encrypt_method = encrypt_method;
        }

        public String getEncrypt_method() {
            return encrypt_method;
        }

        public void setBssid(String bssid) {
            this.bssid = bssid;
        }

        public String getBssid() {
            return bssid;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setDatapoint(RequestEntity.Datapoint datapoint) {
            this.datapoint = datapoint;
        }

        public RequestEntity.Datapoint getDatapoint() {
            return datapoint;
        }

        public String getOfflineData() {
            return offlineData;
        }

        public void setOfflineData(String offlineData) {
            this.offlineData = offlineData;
        }
    }

    public static class Datapoint implements Serializable {

        private static final long serialVersionUID = 1L;

        private String x;// 温度,因为硬件上传负数格式为 -0.-3 会多一个负号，因此用string类型
        private Float y;// 湿度

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        public Float getY() {
            return y;
        }

        public void setY(Float y) {
            this.y = y;
        }
    }

}
