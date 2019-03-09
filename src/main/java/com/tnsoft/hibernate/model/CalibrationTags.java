package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "calibration_tags")
public class CalibrationTags implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int calibrationId;
    private String tagNo;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "calibration_tags_id_seq")
    @SequenceGenerator(name = "calibration_tags_id_seq", sequenceName = "calibration_tags_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "calibration_id")
    public int getCalibrationId() {
        return calibrationId;
    }

    public void setCalibrationId(int calibrationId) {
        this.calibrationId = calibrationId;
    }

    @Column(name = "tag_no")
    public String getTagNo() {
        return tagNo;
    }

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }
}
