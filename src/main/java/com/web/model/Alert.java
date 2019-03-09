package com.web.model;

import com.tnsoft.hibernate.model.Express;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Alert implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private int domainId;
    private String tagNo;
    private int alertLevel;
    private Date creationTime;
    private Date lastModitied;
    private int status;
    private long csn;
    private Integer type;


    private List<Express> express;

    public Alert() {
        super();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }

    public String getTagNo() {
        return tagNo;
    }

    public void setAlertLevel(int alertLevel) {
        this.alertLevel = alertLevel;
    }

    public int getAlertLevel() {
        return alertLevel;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setLastModitied(Date lastModitied) {
        this.lastModitied = lastModitied;
    }

    public Date getLastModitied() {
        return lastModitied;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setCsn(long csn) {
        this.csn = csn;
    }

    public long getCsn() {
        return csn;
    }

    public void setExpress(List<Express> express) {
        this.express = express;
    }

    public List<Express> getExpress() {
        return express;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
