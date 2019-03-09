package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "tag_calibration_upload")
public class TagCalibrationUpload implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String tagNo;
    private float temperature;
    private float humidity;
    private Date time;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "tag_calibration_upload_id_seq")
    @SequenceGenerator(name = "tag_calibration_upload_id_seq", sequenceName = "tag_calibration_upload_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "tag_no")
    public String getTagNo() {
        return tagNo;
    }

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }

    @Column(name = "temp")
    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    @Column(name = "humidity")
    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    @Column(name = "time")
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
