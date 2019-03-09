package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "calibration")
public class Calibration implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private Date creationTime;
    private Date lowTempStartTime;
    private Date mediumTempStartTime;
    private Date highTempStartTime;
    private Date endTime;
    private Float lowTemp;
    private Float mediumTemp;
    private Float highTemp;
    private Float lowHumidity;
    private Float mediumHumidity;
    private Float highHumidity;

    private int status;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "calibration_id_seq")
    @SequenceGenerator(name = "calibration_id_seq", sequenceName = "calibration_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "low_temp_start_time")
    public Date getLowTempStartTime() {
        return lowTempStartTime;
    }

    public void setLowTempStartTime(Date lowTempStartTime) {
        this.lowTempStartTime = lowTempStartTime;
    }

    @Column(name = "medium_temp_start_time")
    public Date getMediumTempStartTime() {
        return mediumTempStartTime;
    }

    public void setMediumTempStartTime(Date mediumTempStartTime) {
        this.mediumTempStartTime = mediumTempStartTime;
    }

    @Column(name = "high_temp_start_time")
    public Date getHighTempStartTime() {
        return highTempStartTime;
    }

    public void setHighTempStartTime(Date highTempStartTime) {
        this.highTempStartTime = highTempStartTime;
    }

    @Column(name = "end_time")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "low_temp")
    public Float getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(Float lowTemp) {
        this.lowTemp = lowTemp;
    }

    @Column(name = "medium_temp")
    public Float getMediumTemp() {
        return mediumTemp;
    }

    public void setMediumTemp(Float mediumTemp) {
        this.mediumTemp = mediumTemp;
    }

    @Column(name = "high_temp")
    public Float getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(Float highTemp) {
        this.highTemp = highTemp;
    }

    @Column(name = "low_humidity")
    public Float getLowHumidity() {
        return lowHumidity;
    }

    public void setLowHumidity(Float lowHumidity) {
        this.lowHumidity = lowHumidity;
    }

    @Column(name = "medium_humidity")
    public Float getMediumHumidity() {
        return mediumHumidity;
    }

    public void setMediumHumidity(Float mediumHumidity) {
        this.mediumHumidity = mediumHumidity;
    }

    @Column(name = "high_humidity")
    public Float getHighHumidity() {
        return highHumidity;
    }

    public void setHighHumidity(Float highHumidity) {
        this.highHumidity = highHumidity;
    }
}
