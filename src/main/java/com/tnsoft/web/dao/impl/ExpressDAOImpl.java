package com.tnsoft.web.dao.impl;

import com.tnsoft.hibernate.model.Express;
import com.tnsoft.web.dao.ExpressDAO;
import com.tnsoft.web.model.Constants;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("expressDAO")
public class ExpressDAOImpl extends BaseDAOImpl<Express> implements ExpressDAO {

    @Override
    public Express getExpressByNo(String expressNo, int domainId) {
        String hql = "from Express where expressNo=? and domainId=?";
        return getOneByHQL(hql, expressNo, domainId);
    }

    @Override
    public List<Express> getByIdList(List<Integer> idList) {
        List<Express> expresses = new ArrayList<>();
        if (null == idList || idList.size() < 1) {
            return expresses;
        }
        for (int id : idList) {
            Express express = getById(id);
            if (null != express) {
                expresses.add(express);
            }
        }
        return expresses;
    }

    @Override
    public List<Express> queryByExpressNo(String expressNo, int domainId) {
        String hql = "from Express where expressNo like '%" + expressNo + "%' and domainId=?";
        return getByHQL(hql, domainId);
    }

    @Override
    public List<Express> getAdminExpressToSign(Integer start, Integer rows, Integer domainId) {
        // TODO Auto-generated method stub
        String hql = "from Express where domainId=? and status<>?";
        return getByHQLWithLimits(start, rows, hql, domainId, Constants.ExpressState.STATE_FINISHED);
    }

    @Override
    public Integer getCountByDomainId(Integer domainId) {
        // TODO Auto-generated method stub
        String hql = "select count(a) from Express a where domainId=? and status<>?";
        return count(hql, domainId, Constants.ExpressState.STATE_FINISHED);
    }

    @Override
    public void saveExpressTemperature(int expressId, Float maxTemp, Float minTemp) {
        if (null != maxTemp && null != minTemp && maxTemp <= minTemp) {
            return;
        }
        Express express = getById(expressId);
        if (null == express) {
            return;
        }
        express.setTemperatureMax(maxTemp);
        express.setTemperatureMin(minTemp);
    }

    @Override
    public void saveExpressSleepTime(Integer expressId, int sleepTime) {
        String hql = "update Express set sleepTime=? where id=?";
        CUDByHql(hql, sleepTime, expressId);
    }

    @Override
    public void saveExpressAppointStart(Integer expressId, Integer appointStart) {
        String hql = "update Express set appointStart=? where id=?";
        CUDByHql(hql, appointStart, expressId);
    }

    @Override
    public void saveExpressDesc(Integer expressId, String desc) {
        String hql = "update Express set description=? where id=?";
        CUDByHql(hql, desc, expressId);
    }

    @Override
    public void saveExpressAppointEnd(Integer expressId, Integer appointEnd) {
        String hql = "update Express set appointEnd=? where id=?";
        CUDByHql(hql, appointEnd, expressId);
    }

    @Override
    public List<Express> getCurrentExpressesByDomainId(int domainId) {
        String hql = "from Express where domainId=? and status<>" + Constants.ExpressState.STATE_FINISHED + " order " +
                "by creationTime desc";
        return getByHQL(hql, domainId);
    }

    @Override
    public List<Express> getHistoryExpressesByDomainId(int domainId) {
        String hql = "from Express where domainId=? and status=" + Constants.ExpressState.STATE_FINISHED + " order by checkOutTime desc";
        return getByHQL(hql, domainId);
    }

}
