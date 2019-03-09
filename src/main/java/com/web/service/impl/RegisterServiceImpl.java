package com.web.service.impl;

import com.aliyuncs.exceptions.ClientException;
import com.tnsoft.hibernate.model.AlertLevel;
import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.dao.AlertLevelDAO;
import com.tnsoft.web.dao.DomainDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserRoleDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.service.RegisterService;
import com.tnsoft.web.util.SmsUtil;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service("registerService")
public class RegisterServiceImpl implements RegisterService {

    @Resource(name = "userDAO")
    private UserDAO userDAO;
    @Resource(name = "domainDAO")
    private DomainDAO domainDAO;
    @Resource(name = "userRoleDAO")
    private UserRoleDAO userRoleDAO;
    @Resource(name = "alertLevelDAO")
    private AlertLevelDAO alertLevelDAO;

    @Override
    public boolean isUsernameAble(String username) {
        if (username != null) {
            username = username.trim();
            if (Utils.isValidUsername(username)) {
                return null == userDAO.getUserByName(username);
            }
        }
        return false;
    }

    @Override
    public boolean isMobileAble(String mobile) {
        if (mobile != null) {
            mobile = mobile.trim();
            return null == userDAO.getUserByMobile(mobile);
        }
        return false;
    }

    private static String getSMScode() {
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        return code + "";
    }

    @Override
    public Result creatNewUserAndDomain(String username, String password, String phone, String roleId) {
        Result result = new Result();
        result.setCode(Result.OK);
        /*if (!isUsernameAble(username)) {
            result.setCode(Result.ERROR);
			result.setMessage("用户名不可用！");
			return result;
		}
		if( !isMobileAble(phone)){
			result.setCode(Result.ERROR);
			result.setMessage("手机号已经被占用！");
			return result;
		}

		if (!Utils.isMobileNO(phone)) {
			result.setCode(Result.ERROR);
			result.setMessage("请输入正确的手机号");
			return result;
		}
		*/
        if (null == password) {
            result.setCode(Result.ERROR);
            result.setMessage("密码不能为空！");
            return result;
        }
        if (password.length() < 6) {
            result.setCode(Result.ERROR);
            result.setMessage("密码不能低于6位！");
            return result;
        }

        int roleIdInt = Integer.parseInt(roleId);


        Date now = new Date();
        //在根节点下创建一个新的站点
        Domain domain = new Domain();
        domain.setAddress("");
        domain.setCreationTime(now);
        domain.setDescription("");
        domain.setDomainPath("/1/");
        domain.setLastModified(now);
        domain.setName(username);
        domain.setPhone(phone);
        domain.setPreferences("");
        domain.setStatus(Constants.DomainState.STATE_ACTIVE);
        if (roleIdInt <= Constants.Role.EXCHANGE_USER) {
            domain.setVersion(Constants.Version.EXPRESS);
        } else if (roleIdInt == Constants.Role.ADMIN_MEDICINE || roleIdInt == Constants.Role.MAINTAINER_MEDICINE || roleIdInt == Constants.Role.SUB_ADMIN_MEDICINE) {
            domain.setVersion(Constants.Version.MEDICINE);
        } else if (roleIdInt == Constants.Role.ADMIN_STANDARD || roleIdInt == Constants.Role.MAINTAINER_STANDARD) {
            domain.setVersion(Constants.Version.STANDARD);
        }
        domainDAO.save(domain);

        AlertLevel alertLevel = new AlertLevel();
        alertLevel.setDomainId(domain.getId());
        alertLevel.setName("严重报警");
        alertLevel.setStatus(Constants.IsAble.DISABLE);
        alertLevel.setCreationTime(now);
        alertLevel.setLastModified(now);
        alertLevel.setType(Constants.AlertLevelType.TEMP_SERIOUS);
        alertLevelDAO.save(alertLevel);

        AlertLevel alertLevel1 = new AlertLevel();
        alertLevel1.setDomainId(domain.getId());
        alertLevel1.setName("失联报警");
        alertLevel1.setStatus(Constants.IsAble.ABLE);
        alertLevel1.setCreationTime(now);
        alertLevel1.setLastModified(now);
        alertLevel1.setHours(1);
        alertLevel1.setTimes(4);
        alertLevel1.setType(Constants.AlertLevelType.NO_RESPONSE);
        alertLevelDAO.save(alertLevel1);

        User user = new User();
        user.setName(username);
        user.setNickName(username);
        user.setMobile(phone);
        user.setPassword(password);

        //其他值设为默认
        user.setAddress("");
        user.setAttempt(0);
        user.setCreationTime(now);
        user.setDescription("");
        user.setGender("男");
        user.setLastModified(now);
        user.setStaffNo("");
        user.setStatus(1);
        user.setType(Integer.parseInt(roleId));
        user.setDomainId(domain.getId());
        user.setRootDomainId(domain.getId());
        userDAO.save(user);

        userRoleDAO.saveUserRole(user.getId(), Integer.parseInt(roleId));
        result.setMessage("注册成功");
        return result;
    }

    @Override
    public Result sendCode(String mobile) {
        Result result = new Result();
        if (!Utils.isMobileNO(mobile)) {
            result.setCode(Result.ERROR);
            result.setMessage("请输入正确的手机号");
            return result;
        }
        String code = getSMScode();
        result.setMessage(code);
        try {
            SmsUtil.sendCodeSms(mobile, code);
        } catch (ClientException e) {
            e.printStackTrace();
        }
        result.setCode(Result.OK);
        return result;
    }

}
