package com.web.dao.impl;

import com.tnsoft.hibernate.model.CalibrationTags;
import com.tnsoft.web.dao.CalibrationTagsDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("calibrationTagsDAO")
public class CalibrationTagsDAOImpl extends BaseDAOImpl<CalibrationTags> implements CalibrationTagsDAO {
    @Override
    public List<CalibrationTags> getByCalibrationId(int id) {
        String hql = "from CalibrationTags where calibrationId=?";
        return getByHQL(hql, id);
    }
}
