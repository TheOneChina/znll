package com.web.dao;

import com.tnsoft.hibernate.model.CalibrationTags;

import java.util.List;

public interface CalibrationTagsDAO extends BaseDAO<CalibrationTags> {

    List<CalibrationTags> getByCalibrationId(int id);
}
