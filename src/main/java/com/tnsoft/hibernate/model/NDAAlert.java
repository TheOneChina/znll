package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_alert")
public class NDAAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int domainId;
    private String tagNo;
    private int alertLevel;
    private Date creationTime;
    private Date lastModitied;
    private Date alertTime;
    private int status;
    private int expressId;
    private Integer type;
    private String statusName;
    private String time;
    private String express;
    private String mobile;
    private String userName;
    private String alertName;

    public NDAAlert() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_alert_id_seq")
    @SequenceGenerator(name = "nda_alert_id_seq", sequenceName = "nda_alert_id_seq")
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

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }

    @Column(name = "tag_no")
    public String getTagNo() {
        return tagNo;
    }

    public void setAlertLevel(int alertLevel) {
        this.alertLevel = alertLevel;
    }

    @Column(name = "nda_alert_level_id")
    public int getAlertLevel() {
        return alertLevel;
    }

    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setLastModitied(Date lastModitied) {
        this.lastModitied = lastModitied;
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

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Transient
    public String getStatusName() {
        return statusName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Transient
    public String getTime() {
        return time;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    @Transient
    public String getExpress() {
        return express;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Transient
    public String getMobile() {
        return mobile;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Transient
    public String getUserName() {
        return userName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    @Transient
    public String getAlertName() {
        return alertName;
    }

    public void setExpressId(int expressId) {
        this.expressId = expressId;
    }

    @Column(name = "express_id")
    public int getExpressId() {
        return expressId;
    }

    @Column(name = "alert_time")
    public Date getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Date alertTime) {
        this.alertTime = alertTime;
    }
}
