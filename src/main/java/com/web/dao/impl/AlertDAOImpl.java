package com.web.dao.impl;

import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.web.dao.AlertDAO;
import com.tnsoft.web.model.Constants;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository("alertDAO")
public class AlertDAOImpl extends BaseDAOImpl<NDAAlert> implements AlertDAO {

    @Override
    public List<NDAAlert> getAlertByTagNo(String tagNo) {
        String hql = "from NDAAlert where tagNo=? and status=?";
        return getByHQL(hql, tagNo, Constants.AlertState.STATE_ACTIVE);
    }

    @Override
    public List<NDAAlert> getAlertsByExpressId(int expressId, int... status) {
        if (expressId < 1) {
            return null;
        }
        List<NDAAlert> list;
        if (status.length < 1) {
            String hql = "from NDAAlert where expressId=?";
            list = getByHQL(hql, expressId);
        } else {
            list = new ArrayList<>();
            for (int i : status) {
                String hql = "from NDAAlert where expressId=? and status=?";
                list.addAll(getByHQL(hql, expressId, i));
            }
        }
        return list;
    }

    @Override
    public NDAAlert getNewestOneByDomainId(int domainId) {
        String hql = "from NDAAlert where domainId=? order by id DESC";
        List<NDAAlert> list = getByHQL(hql, domainId);
        if (null == list || list.size() < 1) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public NDAAlert getNewestOneByExpressId(int expressId) {
        String hql = "from NDAAlert where expressId=? order by id DESC";
        List<NDAAlert> list = getByHQL(hql, expressId);
        if (null == list || list.size() < 1) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public int countAlerts(int expressId, int... status) {
        if (status.length < 1) {
            String hql = "from NDAAlert where expressId=?";
            return count(hql, expressId);
        } else {
            int ans = 0;
            for (int i : status) {
                String hql = "from NDAAlert where expressId=? and status=?";
                ans += count(hql, expressId, i);
            }
            return ans;
        }
    }

    @Override
    public int countAlertsByTime(int expresId, Float hour) {
        if (expresId < 1 || null == hour) {
            return 0;
        }

        List<NDAAlert> alertList = getAlertsByExpressId(expresId, Constants.AlertState.STATE_ACTIVE);
        long now = new Date().getTime();
        now = now - (long) (hour * 3660 * 1000);
        int count = 0;
        for (NDAAlert alert : alertList) {
            if (null != alert.getAlertTime() && now < alert.getAlertTime().getTime()) {
                count++;
            }
        }
        return count;
    }

}
