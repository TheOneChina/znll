package com.tnsoft.hibernate.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "role_permission")
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int roleId;
    private int permissionId;
    private int status;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "role_permission_id_seq")
    @SequenceGenerator(name = "role_permission_id_seq", sequenceName = "role_permission_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "role_id")
    public int getRoleId() {
        return roleId;
    }

    @Column(name = "permission_id")
    public int getPermissionId() {
        return permissionId;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
