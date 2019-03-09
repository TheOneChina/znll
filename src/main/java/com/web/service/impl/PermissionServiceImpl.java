package com.web.service.impl;

import com.tnsoft.hibernate.model.Permission;
import com.tnsoft.hibernate.model.RolePermission;
import com.tnsoft.web.dao.PermissionDAO;
import com.tnsoft.web.dao.RolePermissionDAO;
import com.tnsoft.web.service.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("permissionService")
public class PermissionServiceImpl extends BaseServiceImpl<Permission> implements PermissionService {

    @Resource(name = "rolePermissionDAO")
    private RolePermissionDAO rolePerDao;

    @Resource(name = "permissionDAO")
    private PermissionDAO perDao;

    @Override
    public List<List<Permission>> getPermission(Integer roleId) {
        // TODO Auto-generated method stub
        // 指针重用,不在遍历的时候创建无用指针
        Permission p = null;
        //要讲父菜单和子菜单一起放入一个list
        List<List<Permission>> menus = new ArrayList<List<Permission>>();
        List<Permission> parent = new ArrayList<Permission>();
        List<Permission> son = new ArrayList<Permission>();
        for (RolePermission rp : rolePerDao.getRolePermissionByRId(roleId)) {
            // 获得权下所有permission菜单的id来加载菜单
            p = perDao.getById(rp.getPermissionId());
            if (p.getPid() == null) {
                parent.add(p);
            } else {
                son.add(p);
            }
            // 指针重用
            p = null;
        }
        menus.add(parent);
        menus.add(son);
        return menus;
    }
}
