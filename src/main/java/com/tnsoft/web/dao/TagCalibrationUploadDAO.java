package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.TagCalibrationUpload;

import java.util.Date;
import java.util.List;

public interface TagCalibrationUploadDAO extends BaseDAO<TagCalibrationUpload> {

    List<TagCalibrationUpload> getByTagNoWithTimeLimit(String tagNo, Date start, Date end);

    long countByTagNoWithTimeLimit(String tagNo, Date start, Date end);

    List<TagCalibrationUpload> getByTagNoWithTimeLimit(String tagNo, Date startTime, Date endTime, int start, int length);
}
