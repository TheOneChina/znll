package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_log")
public class OperateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String userName;
    private String operation;
    private String time;
    private Date operationTime;
    private int domainId;

    public OperateLog() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_log_id_seq")
    @SequenceGenerator(name = "nda_log_id_seq", sequenceName = "nda_log_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "user_name")
    public String getUserName() {
        return userName;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Column(name = "operation")
    public String getOperation() {
        return operation;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Transient
    public String getTime() {
        return time;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "operation_time")
    public Date getOperationTime() {
        return operationTime;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    @Column(name = "domain_id")
    public int getDomainId() {
        return domainId;
    }

    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
