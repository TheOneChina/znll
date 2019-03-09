package com.web.service.impl;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.hibernate.model.UserRole;
import com.tnsoft.web.dao.DomainDAO;
import com.tnsoft.web.dao.RoleDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserRoleDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.service.UserService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("userService")
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    @Resource(name = "userDAO")
    private UserDAO userDao;
    @Resource(name = "roleDAO")
    private RoleDAO roleDao;
    @Resource(name = "userRoleDAO")
    private UserRoleDAO userRoleDao;
    @Resource()
    private DomainDAO domainDAO;

    @Override
    public Integer getDomainIdByUserId(Integer userId) {
        Integer domainId = null;
        if (null != userId && userId > 0) {
            User user = userDao.getById(userId);
            if (null != user && user.getDomainId() > 0) {
                domainId = user.getDomainId();
            }
        }
        return domainId;
    }

    @Override
    public List<User> getUserListByUserId(int userId) {
        User user = getById(userId);
        if (null == user) {
            return null;
        }
        Domain domain = domainDAO.getById(user.getDomainId());
        if (null == domain) {
            return null;
        }
        String domainPath = domain.getDomainPath() + domain.getId() + "/";
        List<User> userList = userDao.getUsersByAdminId(userId);
        if (null == userList) {
            userList = new ArrayList<>();
        }
        List<UserRole> allAdmin = userRoleDao.getAllAdmin();
        if (null != allAdmin && allAdmin.size() > 0) {
            for (UserRole ur : allAdmin) {
                User userTemp = getById(ur.getUserId());
                Domain domainTemp = domainDAO.getById(userTemp.getDomainId());
                if (null != domainTemp && domainTemp.getDomainPath().equals(domainPath)) {
                    userList.add(userTemp);
                }
            }
        }
        return userList;
    }

    @Override
    public User getUSerByUserName(String userName) {
        return userDao.getUserByName(userName);
    }


    @Override
    public Result updateUser(String nickName, int id, int roleId, String staffNo, String gender, String mobile,
                             String address, String description) {
        Result result = new Result(Result.ERROR);

        User user = userDao.getById(id);
        if (null == user) {
            result.setMessage("用户不存在！");
            return result;
        }
        if (!StringUtils.isEmpty(nickName)) {
            user.setNickName(nickName);
        }
        if (roleId > 1) {
            userRoleDao.updateUserRole(id, roleId);
            user.setType(roleId);
        }
        if (!StringUtils.isEmpty(staffNo)) {
            user.setStaffNo(staffNo);
        }
        if (!StringUtils.isEmpty(gender)) {
            user.setGender(gender);
        }
        if (!StringUtils.isEmpty(mobile)) {
            user.setMobile(mobile);
        }
        if (!StringUtils.isEmpty(address)) {
            user.setAddress(address);
        }
        if (!StringUtils.isEmpty(description)) {
            user.setDescription(description);
        }
        result.setCode(Result.OK);
        result.setMessage("设置成功！");
        return result;
    }

    @Override
    public Result createUser(String name, String nickName, int roleId, String staffNo, String gender, String
            mobile, String address, String description, int domainId) {

        Date now = new Date();
        User user = new User();
        user.setName(name);
        if (StringUtils.isEmpty(nickName)) {
            user.setNickName(name);
        } else {
            user.setNickName(nickName);
        }
        user.setStaffNo(staffNo);
        user.setGender(gender);
        user.setMobile(mobile);
        user.setAddress(address);
        user.setType(roleId);
        user.setDescription(description);
        user.setCreationTime(now);
        user.setLastModified(now);
        user.setStatus(Constants.State.STATE_ACTIVE);

        Domain fatherDomain = domainDAO.getById(domainId);
        int rootDomainId;
        if (fatherDomain.getDomainPath().length() < 4) {
            rootDomainId = domainId;
        } else {
            String path = fatherDomain.getDomainPath().substring(3);
            String[] split = path.split("/");
            rootDomainId = Integer.parseInt(split[0]);
        }

        if (roleId == Constants.Role.ADMIN || roleId == Constants.Role.ADMIN_MEDICINE || roleId == Constants.Role
                .ADMIN_STANDARD || roleId == Constants.Role.SUB_ADMIN_MEDICINE) {

            //在当前节点下创建一个新的站点
            Domain domain = new Domain();
            domain.setAddress("");
            domain.setCreationTime(now);
            domain.setDescription("");
            domain.setDomainPath(fatherDomain.getDomainPath() + domainId + "/");
            domain.setLastModified(now);
            domain.setName(name);
            domain.setPhone(mobile);
            domain.setPreferences(fatherDomain.getPreferences());
            domain.setStatus(Constants.DomainState.STATE_ACTIVE);
            if (roleId == Constants.Role.ADMIN) {
                domain.setVersion(Constants.Version.EXPRESS);
            } else if (roleId == Constants.Role.ADMIN_MEDICINE || roleId == Constants.Role.SUB_ADMIN_MEDICINE) {
                domain.setVersion(Constants.Version.MEDICINE);
            } else {
                domain.setVersion(Constants.Version.STANDARD);
            }
            domainDAO.save(domain);
            user.setDomainId(domain.getId());
            user.setRootDomainId(rootDomainId);

        } else {
            user.setDomainId(domainId);
            user.setRootDomainId(rootDomainId);
        }

        if (mobile.length() > 6) {
            user.setPassword(mobile.substring(mobile.length() - 6));
        } else {
            user.setPassword("123456");
        }
        userDao.save(user);
        // 新增用户角色表
        userRoleDao.saveUserRole(user.getId(), roleId);


        Result result = new Result(Result.OK);
        result.setMessage("新增用户成功！");
        return result;
    }


    @Override
    public String savePwd(String id, String oldpwd, String newpwd) {
        Result result = new Result();
        Date now = new Date();
        // 当id存在时,说明该用户是在编辑操作
        if (!Utils.isValidUserPasswd(newpwd)) {
            result.setCode(Result.ERROR);
            result.setMessage("新密码格式错误！");
            return Utils.GSON.toJson(result);
        }

        User user = userDao.getById(Integer.parseInt(id));
        String pwd1 = user.getPassword();
        if (pwd1.equals(oldpwd)) {
            user.setPassword(newpwd);
        } else {
            result.setCode(Result.ERROR);
            result.setMessage("原密码输入错误！");
            return Utils.GSON.toJson(result);
        }
        user.setLastModified(now);
        result.setCode(Result.OK);
        result.setMessage("密码修改成功！");
        return Utils.GSON.toJson(result);
    }

    @Override
    public String resetPwd(String id, String account, String newpwd) {
        Result result = new Result();
        Date now = new Date();
        // 当id存在时,说明该用户是在编辑操作
        if (!Utils.isValidUserPasswd(newpwd)) {
            result.setCode(Result.ERROR);
            result.setMessage("新密码格式错误！");
            return Utils.GSON.toJson(result);
        }
        User user = userDao.getUserByName(account);
        if (user == null) {
            result.setCode(Result.ERROR);
            result.setMessage("该用户名不存在！");
            return Utils.GSON.toJson(result);
        } else {
            user.setPassword(newpwd);
        }
        user.setLastModified(now);
        result.setCode(Result.OK);
        result.setMessage("密码重置成功！");
        return Utils.GSON.toJson(result);
    }

    @Override
    public Response savePwdWithoutOldPwd(int userId, String password) {

        Response response = new Response(Response.ERROR);
        User user = userDao.getById(userId);
        if (null == user) {
            response.setMessage("用户不存在！");
            return response;
        }
        if (!Utils.isValidUserPasswd(password)) {
            response.setMessage("新密码格式错误！");
            return response;
        }
        user.setPassword(password);
        response.setCode(Response.OK);
        return response;
    }

    @Override
    public List<Role> getUserRole(Integer userId) {
        return userDao.getUserRole(userId);
    }

    @Override
    public List<Role> getRoles(LoginSession lg) {

        List<Role> list;
        if (lg.getDomain().getDomainPath().equals("/")) {
            list = roleDao.getRoles();
        } else {
            list = roleDao.getAdminRoles();
        }
        return list;
    }

    @Override
    public List<Role> getRoles(int roleId) {
        List<Role> list;
        list = roleDao.getRoles(roleId);

        return list;
    }

    @Override
    public List<Role> getDomainRolesByAdminRoleIdAndDomainId(Integer roleId, Integer domainId) {
        if (null == roleId || null == domainId) {
            return null;
        }
        return roleDao.getDomainRolesByAdminRoleIdAndDomainId(roleId, domainId);
    }

}
