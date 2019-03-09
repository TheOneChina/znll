package com.tnsoft.web.service.impl;

import com.tnsoft.hibernate.model.LocateExpress;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.service.MapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("mapService")
public class MapServiceImpl extends BaseServiceImpl<LocateExpress> implements MapService {

    @Resource(name = "userDAO")
    private UserDAO userDAO;

    @Override
    public List<LocateExpress> getLocationByExpressId(Integer expressId) {
        if (null == expressId) {
            return null;
        }
        String hql = "from LocateExpress where expressId=? order by creationTime ASC";
        return getByHQL(hql, expressId);
    }

    @Override
    public void saveLocation(int userId, double lat, double lng, List<Integer> expressIds) {
        User user = userDAO.getById(userId);
        if (null == user) {
            return;
        }
        if (null != expressIds && expressIds.size() > 0) {
            for (int expressId : expressIds) {
                LocateExpress locateExpress = new LocateExpress();
                locateExpress.setCreationTime(new Date());
                locateExpress.setLat(lat);
                locateExpress.setLng(lng);
                locateExpress.setExpressId(expressId);
                locateExpress.setDomainId(user.getDomainId());
                locateExpress.setName(user.getName());
                locateExpress.setLastModitied(new Date());
                save(locateExpress);
            }
        }
    }
}
