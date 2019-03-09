package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.Calibration;

import java.util.List;

public interface CalibrationDAO extends BaseDAO<Calibration> {

    List<Calibration> getAllDESC();
}
