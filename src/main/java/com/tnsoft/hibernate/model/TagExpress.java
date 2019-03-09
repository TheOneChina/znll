package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_tag_express")
public class TagExpress implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int domainId;
    private int expressId;
    private String tagNo;
    //    private Integer tagStatus;
    private Date creationTime;
    private Date lastModified;
    private int status;

    private String expressNo;
    //    private Integer expressStatus;
//    private Date checkOutTime;
    private String statusName;

    public TagExpress() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_tag_express_id_seq")
    @SequenceGenerator(name = "nda_tag_express_id_seq", sequenceName = "nda_tag_express_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

//    @Transient
//    public Integer getTagStatus() {
//        return tagStatus;
//    }
//
//    public void setTagStatus(Integer tagStatus) {
//        this.tagStatus = tagStatus;
//    }

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

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }

    @Column(name = "tag_no")
    public String getTagNo() {
        return tagNo;
    }

//    @Transient
//    public Date getCheckOutTime() {
//        return checkOutTime;
//    }
//
//    public void setCheckOutTime(Date checkOutTime) {
//        this.checkOutTime = checkOutTime;
//    }
//
//    @Transient
//    public Integer getExpressStatus() {
//        return expressStatus;
//    }
//
//    public void setExpressStatus(Integer expressStatus) {
//        this.expressStatus = expressStatus;
//    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    @Transient
    public String getExpressNo() {
        return expressNo;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Transient
    public String getStatusName() {
        return statusName;
    }


}
