package com.web.model;

import java.io.Serializable;

/**
 * 服务器回复硬件的数据体
 */
public class UploadResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private int status;
    private Datapoint datapoint;

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setDatapoint(UploadResponse.Datapoint datapoint) {
        this.datapoint = datapoint;
    }

    public UploadResponse.Datapoint getDatapoint() {
        return datapoint;
    }

    public UploadResponse() {
        super();
    }

    public class Datapoint {
        private String created;
        private String y;
        private String x;
        private String dstime;
        private String change;
        /**
         * 蜂鸣器开关
         */
        private String buzzer;

        //因硬件按位取值，需将float转为固定位数的String.

        /**
         * 温度校准
         */
        private String temPrecision;
        /**
         * 湿度校准
         */
        private String humPrecision;
        /**
         * 是否绑定订单
         */
        private String tagIsBind;
        /**
         * 是否存在Flash读写错误
         */
        private String FlashErr;
        private String tmax;
        private String tmin;
        private String hmax;
        private String hmin;


        public String getTmax() {
            return tmax;
        }

        public void setTmax(String tmax) {
            this.tmax = tmax;
        }

        public String getTmin() {
            return tmin;
        }

        public void setTmin(String tmin) {
            this.tmin = tmin;
        }

        public String getHmax() {
            return hmax;
        }

        public void setHmax(String hmax) {
            this.hmax = hmax;
        }

        public String getHmin() {
            return hmin;
        }

        public void setHmin(String hmin) {
            this.hmin = hmin;
        }

        public String getBuzzer() {
            return buzzer;
        }

        public void setBuzzer(String buzzer) {
            this.buzzer = buzzer;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getCreated() {
            return created;
        }

        public void setY(String y) {
            this.y = y;
        }

        public String getY() {
            return y;
        }

        public void setX(String x) {
            this.x = x;
        }

        public String getX() {
            return x;
        }

        public void setDstime(String dstime) {
            this.dstime = dstime;
        }

        public String getDstime() {
            return dstime;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getTemPrecision() {
            return temPrecision;
        }

        public void setTemPrecision(String temPrecision) {
            this.temPrecision = temPrecision;
        }

        public String getHumPrecision() {
            return humPrecision;
        }

        public void setHumPrecision(String humPrecision) {
            this.humPrecision = humPrecision;
        }

        public String getTagIsBind() {
            return tagIsBind;
        }

        public void setTagIsBind(String tagIsBind) {
            this.tagIsBind = tagIsBind;
        }

        public String getFlashErr() {
            return FlashErr;
        }

        public void setFlashErr(String flashErr) {
            FlashErr = flashErr;
        }
    }
}
