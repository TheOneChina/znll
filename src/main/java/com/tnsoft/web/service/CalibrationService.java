package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.Calibration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CalibrationService {

    List<Calibration> getAll();

    boolean createCalibrationTask(String taskname, Set<String> tagNos);

    List<String> getTagsByCalibrationId(int id);

    boolean saveStandardValue(int id, int flag, Float temp, Float humidity);

    boolean nextTaskStep(int id);

    void calculateCalibration(int id);

    Map<String, Object> getTagCalibrationUpload(int start, int length, String tagNo, int id);
}
