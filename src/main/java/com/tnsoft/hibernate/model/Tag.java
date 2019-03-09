package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author z
 */
@Entity
@Table(name = "nda_tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tagNo;
    private Integer domainId;
    private Integer electricity;
    private Date creationTime;
    private Date lastModitied;
    private String electricityStatus;

    private String bSSID;
    private Integer status;
    private String token;
    private String name;
    /**
     * 设备wifi的开关状态
     */
    private Integer wifiStatus;

    private String statusName;


    // 设备可设置参数

    /**
     * 温度校准值
     */
    private Float precision;
    private Float precisionNow;

    // 湿度校准值
    private Float hPrecision;
    private Float hPrecisionNow;

    // 上传周期，以分钟为单位
    private Integer sleepTime;// 设备设置
    private Integer expressSleepTime;// 订单上传周期
    // 蜂鸣器
    private Integer buzzer;
    private Integer buzzerNow;
    // 温度上下限
    private Float temperatureMin;
    private Float temperatureMax;
    private Float temperatureMinNow;
    private Float temperatureMaxNow;
    //绑定的订单上下限设置
    private Float expressTMin;
    private Float expressTMax;

    // 湿度上下限
    private Float humidityMin;
    private Float humidityMax;
    private Float humidityMinNow;
    private Float humidityMaxNow;
    //绑定的订单上下限设置
    private Float expressHMin;
    private Float expressHMax;

    // WiFi账号密码
    private String SSID;
    private String password;
    private String SSIDNow;
    private String passwordNow;

    // 延时启动
    private Integer appointStart;

    //受警人号码
    private String alertPhones;

    //设备MAC地址
    private String MAC;

    //设备剩余短信数
    private int sms;

    //设备最后短信报警时间
    private Date lastSMSAlert;

    /**
     * 设备最后通信时间
     */
    private Date lastConnected;

    /**
     * 设备开始使用时间
     */
    private Date startUseTime;

    /**
     * 设备平台服务费到期时间
     */
    private Date serviceExpirationTime;

    //设备硬件类型：0无屏幕， 1有屏幕
    private int hardwareType;
    //设备软件类型,默认为空：1物流 2医药 3标准
    private Integer softwareType;

    //    是否处于校准状态
//    暂定0 非校准； 1 接收校准数据中； 2 校准结束，待计算结果
    private int calibrationStatus;

    //校准时间
    private Date calibrationTime;
    private float calibrationLowTemp;
    private float calibrationMediumTemp;
    private float calibrationHighTemp;
    private float calibrationHumidity;

    //测量标准值
    private float standardLowTemp;
    private float standardMediumTemp;
    private float standardHighTemp;
    private float standardHumidity;

    private int calibrationType;


    public Tag() {
    }

    public Tag(String tagNo, String name) {
        this.tagNo = tagNo;
        this.name = name;
    }

    @Column(name = "express_t_min")
    public Float getExpressTMin() {
        return expressTMin;
    }

    public void setExpressTMin(Float expressTMin) {
        this.expressTMin = expressTMin;
    }

    @Column(name = "express_t_max")
    public Float getExpressTMax() {
        return expressTMax;
    }

    public void setExpressTMax(Float expressTMax) {
        this.expressTMax = expressTMax;
    }

    @Column(name = "express_h_min")
    public Float getExpressHMin() {
        return expressHMin;
    }

    public void setExpressHMin(Float expressHMin) {
        this.expressHMin = expressHMin;
    }

    @Column(name = "express_h_max")
    public Float getExpressHMax() {
        return expressHMax;
    }

    public void setExpressHMax(Float expressHMax) {
        this.expressHMax = expressHMax;
    }

    @Column(name = "h_precision")
    public Float gethPrecision() {
        return hPrecision;
    }

    public void sethPrecision(Float hPrecision) {
        this.hPrecision = hPrecision;
    }

    @Column(name = "h_precision_now")
    public Float gethPrecisionNow() {
        return hPrecisionNow;
    }

    public void sethPrecisionNow(Float hPrecisionNow) {
        this.hPrecisionNow = hPrecisionNow;
    }

    @Column(name = "wifi_status")
    public Integer getWifiStatus() {
        return wifiStatus;
    }

    public void setWifiStatus(Integer wifiStatus) {
        this.wifiStatus = wifiStatus;
    }

    @Column(name = "humidity_min")
    public Float getHumidityMin() {
        return humidityMin;
    }

    public void setHumidityMin(Float humidityMin) {
        this.humidityMin = humidityMin;
    }

    @Column(name = "humidity_max")
    public Float getHumidityMax() {
        return humidityMax;
    }

    public void setHumidityMax(Float humidityMax) {
        this.humidityMax = humidityMax;
    }

    @Column(name = "humidity_min_now")
    public Float getHumidityMinNow() {
        return humidityMinNow;
    }

    public void setHumidityMinNow(Float humidityMinNow) {
        this.humidityMinNow = humidityMinNow;
    }

    @Column(name = "humidity_max_now")
    public Float getHumidityMaxNow() {
        return humidityMaxNow;
    }

    public void setHumidityMaxNow(Float humidityMaxNow) {
        this.humidityMaxNow = humidityMaxNow;
    }

    @Column(name = "express_sleep_time")
    public Integer getExpressSleepTime() {
        return expressSleepTime;
    }

    public void setExpressSleepTime(Integer expressSleepTime) {
        this.expressSleepTime = expressSleepTime;
    }

    @Column(name = "buzzer_now")
    public Integer getBuzzerNow() {
        return buzzerNow;
    }

    public void setBuzzerNow(Integer buzzerNow) {
        this.buzzerNow = buzzerNow;
    }

    @Column(name = "temperature_min_now")
    public Float getTemperatureMinNow() {
        return temperatureMinNow;
    }

    public void setTemperatureMinNow(Float temperatureMinNow) {
        this.temperatureMinNow = temperatureMinNow;
    }

    @Column(name = "temperature_max_now")
    public Float getTemperatureMaxNow() {
        return temperatureMaxNow;
    }

    public void setTemperatureMaxNow(Float temperatureMaxNow) {
        this.temperatureMaxNow = temperatureMaxNow;
    }

    @Column(name = "ssid_now")
    public String getSSIDNow() {
        return SSIDNow;
    }

    public void setSSIDNow(String sSIDNow) {
        SSIDNow = sSIDNow;
    }

    @Column(name = "password_now")
    public String getPasswordNow() {
        return passwordNow;
    }

    public void setPasswordNow(String passwordNow) {
        this.passwordNow = passwordNow;
    }

    @Id
    @Column(name = "tag_no", nullable = false)
    public String getTagNo() {
        return tagNo;
    }

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }

    @Column(name = "domain_id")
    public Integer getDomainId() {
        return domainId;
    }

    public void setDomainId(Integer domainId) {
        this.domainId = domainId;
    }

    @Column(name = "appoint_start")
    public Integer getAppointStart() {
        return appointStart;
    }

    public void setAppointStart(Integer appointStart) {
        this.appointStart = appointStart;
    }

    @Column(name = "temperature_min")
    public Float getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(Float temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    @Column(name = "temperature_max")
    public Float getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(Float temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModitied() {
        return lastModitied;
    }

    public void setLastModitied(Date lastModitied) {
        this.lastModitied = lastModitied;
    }

    @Column(name = "electricity")
    public Integer getElectricity() {
        return electricity;
    }

    public void setElectricity(Integer electricity) {
        this.electricity = electricity;
    }

    @Column(name = "buzzer")
    public Integer getBuzzer() {
        return buzzer;
    }

    public void setBuzzer(Integer buzzer) {
        this.buzzer = buzzer;
    }

    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Transient
    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Column(name = "token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Column(name = "bssid")
    public String getBSSID() {
        return bSSID;
    }

    public void setBSSID(String bSSID) {
        this.bSSID = bSSID;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "sleep_time")
    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Column(name = "precision")
    public Float getPrecision() {
        return precision;
    }

    public void setPrecision(Float precision) {
        this.precision = precision;
    }

    @Column(name = "ssid")
    public String getSSID() {
        return SSID;
    }

    public void setSSID(String sSID) {
        SSID = sSID;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Column(name = "precision_now")
    public Float getPrecisionNow() {
        return precisionNow;
    }

    public void setPrecisionNow(Float precisionNow) {
        this.precisionNow = precisionNow;
    }

    @Transient
    public String getElectricityStatus() {
        return electricityStatus;
    }

    public void setElectricityStatus(String electricityStatus) {
        this.electricityStatus = electricityStatus;
    }

    @Column(name = "alert_phones")
    public String getAlertPhones() {
        return alertPhones;
    }

    public void setAlertPhones(String alertPhones) {
        this.alertPhones = alertPhones;
    }

    @Column(name = "mac")
    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    @Column(name = "sms")
    public int getSms() {
        return sms;
    }

    public void setSms(int sms) {
        this.sms = sms;
    }

    @Column(name = "last_connected")
    public Date getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(Date lastConnected) {
        this.lastConnected = lastConnected;
    }

    @Column(name = "last_sms_alert")
    public Date getLastSMSAlert() {
        return lastSMSAlert;
    }

    public void setLastSMSAlert(Date lastSMSAlert) {
        this.lastSMSAlert = lastSMSAlert;
    }

    @Column(name = "start_use_time")
    public Date getStartUseTime() {
        return startUseTime;
    }

    public void setStartUseTime(Date startUseTime) {
        this.startUseTime = startUseTime;
    }

    @Column(name = "hardware_type")
    public int getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(int hardwareType) {
        this.hardwareType = hardwareType;
    }

    @Column(name = "software_type")
    public Integer getSoftwareType() {
        return softwareType;
    }

    public void setSoftwareType(Integer softwareType) {
        this.softwareType = softwareType;
    }

    @Column(name = "calibration")
    public int getCalibrationStatus() {
        return calibrationStatus;
    }

    public void setCalibrationStatus(int calibrationStatus) {
        this.calibrationStatus = calibrationStatus;
    }

    @Column(name = "calibration_time")
    public Date getCalibrationTime() {
        return calibrationTime;
    }

    public void setCalibrationTime(Date calibrationTime) {
        this.calibrationTime = calibrationTime;
    }

    @Column(name = "calibration_low_temp")
    public float getCalibrationLowTemp() {
        return calibrationLowTemp;
    }

    public void setCalibrationLowTemp(float calibrationLowTemp) {
        this.calibrationLowTemp = calibrationLowTemp;
    }

    @Column(name = "calibration_medium_temp")
    public float getCalibrationMediumTemp() {
        return calibrationMediumTemp;
    }

    public void setCalibrationMediumTemp(float calibrationMediumTemp) {
        this.calibrationMediumTemp = calibrationMediumTemp;
    }

    @Column(name = "calibration_high_temp")
    public float getCalibrationHighTemp() {
        return calibrationHighTemp;
    }

    public void setCalibrationHighTemp(float calibrationHighTemp) {
        this.calibrationHighTemp = calibrationHighTemp;
    }

    @Column(name = "calibration_humidity")
    public float getCalibrationHumidity() {
        return calibrationHumidity;
    }

    public void setCalibrationHumidity(float calibrationHumidity) {
        this.calibrationHumidity = calibrationHumidity;
    }

    @Column(name = "calibration_type")
    public int getCalibrationType() {
        return calibrationType;
    }

    public void setCalibrationType(int calibrationType) {
        this.calibrationType = calibrationType;
    }

    @Column(name = "standard_low_temp")
    public float getStandardLowTemp() {
        return standardLowTemp;
    }

    public void setStandardLowTemp(float standardLowTemp) {
        this.standardLowTemp = standardLowTemp;
    }

    @Column(name = "standard_medium_temp")
    public float getStandardMediumTemp() {
        return standardMediumTemp;
    }

    public void setStandardMediumTemp(float standardMediumTemp) {
        this.standardMediumTemp = standardMediumTemp;
    }

    @Column(name = "standard_high_temp")
    public float getStandardHighTemp() {
        return standardHighTemp;
    }

    public void setStandardHighTemp(float standardHighTemp) {
        this.standardHighTemp = standardHighTemp;
    }

    @Column(name = "standard_humidity")
    public float getStandardHumidity() {
        return standardHumidity;
    }

    public void setStandardHumidity(float standardHumidity) {
        this.standardHumidity = standardHumidity;
    }

    @Column(name = "service_expiration_time")
    public Date getServiceExpirationTime() {
        return serviceExpirationTime;
    }

    public void setServiceExpirationTime(Date serviceExpirationTime) {
        this.serviceExpirationTime = serviceExpirationTime;
    }
}
