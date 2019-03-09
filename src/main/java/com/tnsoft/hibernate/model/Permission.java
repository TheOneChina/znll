package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private String url;
    private Integer pid;
    private String method;
    private String icon;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "permission_id_seq")
    @SequenceGenerator(name = "permission_id_seq", sequenceName = "permission_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    @Column(name = "pid")
    public Integer getPid() {
        return pid;
    }

    @Column(name = "method")
    public String getMethod() {
        return method;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Column(name = "icon")
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Permission [id=" + id + ", name=" + name + ", description=" + description + ", url=" + url + ", pid="
                + pid + ", method=" + method + ", icon=" + icon + "]";
    }

}
