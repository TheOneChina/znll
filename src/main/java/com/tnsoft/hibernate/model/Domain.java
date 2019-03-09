package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "nda_domain")
public class Domain implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    //    private byte[] password;
//    private String displayName;
    private String description;
    private String email;
    private String phone;
    private String fax;
    private Integer iconId;
    private String address;
    private String preferences;
    private Date creationTime;
    private Date lastModified;
    private Date lastLogin;
    private int status;
    private String domainPath;
    //站点属于什么版本
    private int version;

    private String statusName;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_domain_id_seq")
    @SequenceGenerator(name = "nda_domain_id_seq", sequenceName = "nda_domain_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

//    @Column(name = "password")
//    public byte[] getPassword() {
//        return password;
//    }
//
//    @Column(name = "display_name")
//    public String getDisplayName() {
//        return displayName;
//    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    @Column(name = "fax")
    public String getFax() {
        return fax;
    }

    @Column(name = "icon_id")
    public Integer getIconId() {
        return iconId;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    @Column(name = "preferences")
    public String getPreferences() {
        return preferences;
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

//    public void setPassword(byte[] password) {
//        this.password = password;
//    }
//
//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
//    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
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

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Transient
    public String getStatusName() {
        return statusName;
    }

    @Column(name = "domain_path")
    public String getDomainPath() {
        return domainPath;
    }

    public void setDomainPath(String domainPath) {
        this.domainPath = domainPath;
    }

    @Column(name = "version")
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
