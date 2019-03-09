package com.tnsoft.web.service.impl;

import com.tnsoft.hibernate.model.Role;
import com.tnsoft.web.dao.RoleDAO;
import com.tnsoft.web.service.RoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("roleService")
public class RoleServiceImpl extends BaseServiceImpl<Role> implements RoleService {

    @Resource(name = "roleDAO")
    private RoleDAO roleDao;

    @Override
    public List<Role> getAllRole() {
        return roleDao.getRoles();
    }

}
