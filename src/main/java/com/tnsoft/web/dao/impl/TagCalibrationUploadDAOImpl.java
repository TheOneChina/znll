package com.tnsoft.web.dao.impl;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.model.TagCalibrationUpload;
import com.tnsoft.web.dao.TagCalibrationUploadDAO;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("tagCalibrationUploadDAO")
public class TagCalibrationUploadDAOImpl extends BaseDAOImpl<TagCalibrationUpload> implements TagCalibrationUploadDAO {
    @Override
    public List<TagCalibrationUpload> getByTagNoWithTimeLimit(String tagNo, Date start, Date end) {
        String hql = "from TagCalibrationUpload where tagNo=\'" + tagNo + "\'";
        if (null != start) {
            hql += " and time >= :start";
        }
        if (null != end) {
            hql += " and time <= :end";
        }
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
    public long countByTagNoWithTimeLimit(String tagNo, Date start, Date end) {
        String hql = "select count(*) from TagCalibrationUpload where tagNo=\'" + tagNo + "\'";
        if (null != start) {
            hql += " and time >= :start";
        }
        if (null != end) {
            hql += " and time <= :end";
        }
        Query query = getSession().createQuery(hql);
        if (start != null) {
            query.setTimestamp("start", start);
        }
        if (end != null) {
            query.setTimestamp("end", end);
        }
        Object ans = query.list().get(0);
        return (long) ans;
    }

    @Override
    public List<TagCalibrationUpload> getByTagNoWithTimeLimit(String tagNo, Date startTime, Date endTime, int start, int length) {
        String hql = "from TagCalibrationUpload where tagNo=\'" + tagNo + "\'";
        if (null != startTime) {
            hql += " and time >= :start";
        }
        if (null != endTime) {
            hql += " and time <= :end";
        }
        Query query = getSession().createQuery(hql);
        if (startTime != null) {
            query.setTimestamp("start", startTime);
        }
        if (endTime != null) {
            query.setTimestamp("end", endTime);
        }
        BaseHibernateUtils.setLimit(query, start, length);
        return query.list();
    }
}
