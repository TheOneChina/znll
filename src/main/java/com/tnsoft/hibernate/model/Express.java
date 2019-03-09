package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_express")
public class Express implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int domainId;
    private String expressNo;
    private Date creationTime;
    private Date lastModitied;
    private Date checkInTime;
    private Date checkOutTime;
    private Date lastDataTime;
    private int status;
    private Float temperatureMin;
    private Float temperatureMax;
    private Integer sleepTime;

    private String statusName;
    private String userName;
    private String description;

    private String creationTimeStr;
    private String checkInTimeStr;
    private String checkOutTimeStr;

    private Integer appointStart;
    private Integer appointEnd;

    private int creatMonitorStatus;
    //未处理报警次数统计
    private int alertCount;
    //已处理报警次数
    private int historyAlertCount;

    //数据是否完整
    private boolean dataComplete;
    private int bindHardType;

    public Express() {
        super();
        this.alertCount = 0;
        this.historyAlertCount = 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_express_id_seq")
    @SequenceGenerator(name = "nda_express_id_seq", sequenceName = "nda_express_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    @Column(name = "domain_id")
    public int getDomainId() {
        return domainId;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    @Column(name = "express_no")
    public String getExpressNo() {
        return expressNo;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setLastModitied(Date lastModified) {
        this.lastModitied = lastModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModitied() {
        return lastModitied;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checkin_time")
    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checkout_time")
    public Date getCheckOutTime() {
        return checkOutTime;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Transient
    public String getStatusName() {
        return statusName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Transient
    public String getUserName() {
        return userName;
    }

    public void setCheckInTimeStr(String checkInTimeStr) {
        this.checkInTimeStr = checkInTimeStr;
    }

    @Column(name = "sleep_time")
    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Transient
    public String getCreationTimeStr() {
        return creationTimeStr;
    }

    public void setCreationTimeStr(String creationTimeStr) {
        this.creationTimeStr = creationTimeStr;
    }

    @Transient
    public String getCheckInTimeStr() {
        return checkInTimeStr;
    }

    public void setCheckOutTimeStr(String checkOutTimeStr) {
        this.checkOutTimeStr = checkOutTimeStr;
    }

    @Transient
    public String getCheckOutTimeStr() {
        return checkOutTimeStr;
    }

    public void setTemperatureMin(Float temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    @Column(name = "temperature_min")
    public Float getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMax(Float temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    @Column(name = "temperature_max")
    public Float getTemperatureMax() {
        return temperatureMax;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "appoint_start")
    public Integer getAppointStart() {
        return appointStart;
    }

    public void setAppointStart(Integer appointStart) {
        this.appointStart = appointStart;
    }

    @Column(name = "appoint_end")
    public Integer getAppointEnd() {
        return appointEnd;
    }

    public void setAppointEnd(Integer appointEnd) {
        this.appointEnd = appointEnd;
    }

    @Transient
    public int getCreatMonitorStatus() {
        return creatMonitorStatus;
    }

    public void setCreatMonitorStatus(int creatMonitorStatus) {
        this.creatMonitorStatus = creatMonitorStatus;
    }

    @Column(name = "alert_count")
    public int getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
    }

    @Transient
    public boolean isDataComplete() {
        return dataComplete;
    }

    public void setDataComplete(boolean dataComplete) {
        this.dataComplete = dataComplete;
    }

    @Column(name = "his_alert_count")
    public int getHistoryAlertCount() {
        return historyAlertCount;
    }

    public void setHistoryAlertCount(int historyAlertCount) {
        this.historyAlertCount = historyAlertCount;
    }

    public void addAlertCount() {
        this.alertCount++;
    }

    @Transient
    public int getBindHardType() {
        return bindHardType;
    }

    public void setBindHardType(int bindHardType) {
        this.bindHardType = bindHardType;
    }

    @Column(name = "last_data_time")
    public Date getLastDataTime() {
        return lastDataTime;
    }

    public void setLastDataTime(Date lastDataTime) {
        this.lastDataTime = lastDataTime;
    }

}
