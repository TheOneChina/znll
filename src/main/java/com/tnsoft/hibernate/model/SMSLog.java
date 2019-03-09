package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "sms_log")
public class SMSLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String tagNo;
    private String mobile;
    private int type;
    private String result;
    private Date time;
    private int domainId;

    public SMSLog() {
        super();
    }

    public SMSLog(String tagNo, String mobile, int type, String result, Date time, int domainId) {
        this.tagNo = tagNo;
        this.mobile = mobile;
        this.type = type;
        this.result = result;
        this.time = time;
        this.domainId = domainId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sms_log_id_seq")
    @SequenceGenerator(name = "sms_log_id_seq", sequenceName = "sms_log_id_seq")
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

    @Column(name = "mobile")
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Column(name = "result")
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time")
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Column(name = "domain_id")
    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }
}
