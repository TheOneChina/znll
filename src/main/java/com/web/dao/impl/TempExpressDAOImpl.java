package com.web.dao.impl;

import com.tnsoft.hibernate.model.TempExpress;
import com.tnsoft.web.dao.TempExpressDAO;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("tempExpressDAO")
public class TempExpressDAOImpl extends BaseDAOImpl<TempExpress> implements TempExpressDAO {

    @Override
    public List<TempExpress> getByExpressId(int expressId) {
        String hql = "from TempExpress where expressId=" + expressId + "order by creationTime ASC";
        return getByHQL(hql);
    }

    @Override
    public TempExpress getNewsetOneByExpressId(int expressId) {
        String hql = "from TempExpress where expressId=" + expressId + "order by creationTime DESC";
        List<TempExpress> list = getByHQL(hql);
        if (null == list || list.size() < 1) {
            return null;
        }
        return list.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TempExpress> getByExpressIdWithTimeLimit(int expressId, Date start, Date end) {
        String hql = "from TempExpress where expressId=" + expressId;
        if (null != start) {
            hql += " and creationTime >= :start";
        }
        if (null != end) {
            hql += " and creationTime <= :end";
        }
        hql += " order by creationTime ASC";
        Query query = getSession().createQuery(hql);
        if (start != null) {
            query.setTimestamp("start", start);
        }
        if (end != null) {
            query.setTimestamp("end", end);
        }
        return query.list();
    }

    @Override
    public boolean hasData(int expressId) {
        String hql = "from TempExpress where expressId=" + expressId;
        Query query = getSession().createQuery(hql);
        List list = query.list();
        if (null != list && list.size() > 0) {
            return true;
        }
        return false;
    }

}
