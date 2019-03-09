package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_temperature_express")
public class TempExpress implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int domainId;
    private int expressId;
    private float temperature;
    private float humidity;
    private Date creationTime;
    private Date lastModitied;

    private String tmpValue;
    private String humidityValue;
    private String timeValue;

    public TempExpress() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_temperature_express_id_seq")
    @SequenceGenerator(name = "nda_temperature_express_id_seq", sequenceName = "nda_temperature_express_id_seq")
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

    public void setExpressId(int expressId) {
        this.expressId = expressId;
    }

    @Column(name = "express_id")
    public int getExpressId() {
        return expressId;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    @Column(name = "temperature")
    public float getTemperature() {
        return temperature;
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

    public void setTmpValue(String tmpValue) {
        this.tmpValue = tmpValue;
    }

    @Transient
    public String getTmpValue() {
        return tmpValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }

    @Transient
    public String getTimeValue() {
        return timeValue;
    }

    @Column(name = "humidity")
    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    @Transient
    public String getHumidityValue() {
        return humidityValue;
    }

    public void setHumidityValue(String humidityValue) {
        this.humidityValue = humidityValue;
    }
}
