package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int type;
    private String password;
    private String ticket;
    private int attempt;
    private String nickName;
    private String gender;
    private String birthDate;
    private String email;
    private String mobile;
    private Integer iconId;
    private Date creationTime;
    private Date lastModified;
    private Date lastLogin;
    private int status;
    private String staffNo;
    private String address;
    private int domainId;
    private int rootDomainId;

    private String statusName;
    private String description;

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_user_id_seq")
    @SequenceGenerator(name = "nda_user_id_seq", sequenceName = "nda_user_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "type")
    public int getType() {
        return type;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    @Column(name = "ticket")
    public String getTicket() {
        return ticket;
    }

    @Column(name = "attempt")
    public int getAttempt() {
        return attempt;
    }

    @Column(name = "nick_name")
    public String getNickName() {
        return nickName;
    }

    @Column(name = "gender")
    public String getGender() {
        return gender;
    }

    @Column(name = "birth_date")
    public String getBirthDate() {
        return birthDate;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    @Column(name = "mobile")
    public String getMobile() {
        return mobile;
    }

    @Column(name = "icon_id")
    public Integer getIconId() {
        return iconId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    public Date getLastLogin() {
        return lastLogin;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    @Column(name = "staff_no")
    public String getStaffNo() {
        return staffNo;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Transient
    public String getStatusName() {
        return statusName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    @Column(name = "domain_id")
    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    @Column(name = "root_domain_id")
    public int getRootDomainId() {
        return rootDomainId;
    }

    public void setRootDomainId(int rootDomainId) {
        this.rootDomainId = rootDomainId;
    }
}
