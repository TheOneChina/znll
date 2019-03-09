package com.web.dao.impl;

import com.tnsoft.hibernate.model.Calibration;
import com.tnsoft.web.dao.CalibrationDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("calibrationDAO")
public class CalibrationDAOImpl extends BaseDAOImpl<Calibration> implements CalibrationDAO {
    @Override
    public List<Calibration> getAllDESC() {
        String hql = "from Calibration order by id DESC";
        return getByHQL(hql);
    }
}
