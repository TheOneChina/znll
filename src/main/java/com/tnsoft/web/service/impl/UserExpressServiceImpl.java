package com.tnsoft.web.service.impl;

import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.hibernate.model.UserExpress;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserExpressDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.service.UserExpressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("userExpressService")
public class UserExpressServiceImpl extends BaseServiceImpl<UserExpress> implements UserExpressService {

    @Resource(name = "userDAO")
    private UserDAO userDAO;
    @Resource(name = "userExpressDAO")
    private UserExpressDAO userExpressDAO;
    @Resource()
    private TagService tagService;

    @Override
    public boolean createUserExpressRelation(Integer userId, Integer expressId) {
        if (null == userId || null == expressId) {
            return false;
        }
        User user = userDAO.getById(userId);
        if (null == user) {
            return false;
        }
        UserExpress userExpress = new UserExpress();
        userExpress.setExpressId(expressId);
        userExpress.setUserId(userId);
        userExpress.setDomainId(user.getDomainId());
        userExpress.setStatus(Constants.UserExpressState.STATE_ACTIVE);
        userExpress.setCreationTime(new Date());
        userExpress.setLastModitied(new Date());

        save(userExpress);
        //更新设备报警联系人
        Tag tag = tagService.getTagByEId(expressId);
        if (null != tag && user.getMobile() != null) {
            tagService.editAlertPhones(tag.getTagNo(), user.getMobile() + ";");
        }

        return true;
    }

    @Override
    public boolean setAllActiveUserExpressToFinishedByEId(Integer expressId) {
        if (null == expressId) {
            return false;
        }
        List<UserExpress> userExpressList = userExpressDAO.getUserExpressByEId(expressId);
        if (null != userExpressList && !userExpressList.isEmpty()) {
            for (UserExpress ue : userExpressList) {
                ue.setStatus(Constants.UserExpressState.STATE_FINISHED);
                ue.setLastModitied(new Date());
            }
            return true;
        }
        return false;
    }

}
