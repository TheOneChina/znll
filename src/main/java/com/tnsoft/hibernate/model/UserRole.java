package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int roleId;
    private int flag;
    private int status;

    public UserRole() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_role_id_seq")
    @SequenceGenerator(name = "user_role_id_seq", sequenceName = "user_role_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    @Column(name = "role_id")
    public int getRoleId() {
        return roleId;
    }

    @Column(name = "flag")
    public int getFlag() {
        return flag;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
