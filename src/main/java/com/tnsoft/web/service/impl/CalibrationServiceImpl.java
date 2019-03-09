package com.tnsoft.web.service.impl;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.Calibration;
import com.tnsoft.hibernate.model.CalibrationTags;
import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.hibernate.model.TagCalibrationUpload;
import com.tnsoft.web.dao.CalibrationDAO;
import com.tnsoft.web.dao.CalibrationTagsDAO;
import com.tnsoft.web.dao.TagCalibrationUploadDAO;
import com.tnsoft.web.dao.TagDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.service.CalibrationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("calibrationService")
public class CalibrationServiceImpl implements CalibrationService {

    @Resource(name = "calibrationDAO")
    private CalibrationDAO calibrationDAO;
    @Resource(name = "tagDAO")
    private TagDAO tagDAO;
    @Resource(name = "calibrationTagsDAO")
    private CalibrationTagsDAO calibrationTagsDAO;
    @Resource(name = "tagCalibrationUploadDAO")
    private TagCalibrationUploadDAO tagCalibrationUploadDAO;

    @Override
    public List<Calibration> getAll() {
        return calibrationDAO.getAllDESC();
    }

    @Override
    public boolean createCalibrationTask(String taskName, Set<String> tagNos) {
        if (null == tagNos || tagNos.size() < 1) {
            return false;
        }
        Calibration calibration = new Calibration();
        calibration.setCreationTime(new Date());
        calibration.setStatus(Constants.Calibrate.TASK_STATUS_CREATION);
        if (!StringUtils.isEmpty(taskName)) {
            calibration.setName(taskName);
        }
        calibrationDAO.save(calibration);
        for (String tagNo : tagNos) {
            Tag tag = tagDAO.getById(tagNo);
            if (null != tag && tag.getCalibrationStatus() == Constants.Calibrate.STATUS_OFF) {
                CalibrationTags calibrationTags = new CalibrationTags();
                calibrationTags.setCalibrationId(calibration.getId());
                calibrationTags.setTagNo(tagNo);
                calibrationTagsDAO.save(calibrationTags);
                tag.setCalibrationStatus(Constants.Calibrate.STATUS_ON);
                tag.setLastModitied(new Date());
                tagDAO.update(tag);
            }
        }
        return true;
    }

    @Override
    public List<String> getTagsByCalibrationId(int id) {
        List<CalibrationTags> calibrationTags = calibrationTagsDAO.getByCalibrationId(id);
        List<String> list = new ArrayList<>();
        if (null != calibrationTags && calibrationTags.size() > 0) {
            for (CalibrationTags tag : calibrationTags) {
                list.add(tag.getTagNo());
            }
        }
        return list;
    }

    @Override
    public boolean saveStandardValue(int id, int flag, Float temp, Float humidity) {
        Calibration calibration = calibrationDAO.getById(id);
        if (null == calibration) {
            return false;
        }
        if (flag == 1) {
            calibration.setLowTemp(temp);
            calibration.setLowHumidity(humidity);
        } else if (flag == 2) {
            calibration.setMediumTemp(temp);
            calibration.setMediumHumidity(humidity);
        } else if (flag == 3) {
            calibration.setHighTemp(temp);
            calibration.setHighHumidity(humidity);
        }
        calibrationDAO.update(calibration);
        return true;
    }

    @Override
    public boolean nextTaskStep(int id) {
        Calibration calibration = calibrationDAO.getById(id);
        if (null == calibration) {
            return false;
        }
        switch (calibration.getStatus()) {
            case 0:
                calibration.setLowTempStartTime(new Date());
                calibration.setStatus(Constants.Calibrate.TASK_STATUS_LOW);
                break;
            case 1:
                calibration.setMediumTempStartTime(new Date());
                calibration.setStatus(Constants.Calibrate.TASK_STATUS_MEDIUM);
                break;
            case 2:
                calibration.setHighTempStartTime(new Date());
                calibration.setStatus(Constants.Calibrate.TASK_STATUS_HIGH);
                break;
            case 3:
                calibration.setEndTime(new Date());
                calibration.setStatus(Constants.Calibrate.TASK_STATUS_END);
                calculateCalibration(id);
                break;
            default:
                break;
        }
        calibrationDAO.update(calibration);
        return true;
    }

    @Override
    public void calculateCalibration(int id) {
        Calibration calibration = calibrationDAO.getById(id);
        if (null == calibration) {
            return;
        }
        List<String> tagsStr = getTagsByCalibrationId(id);
        if (null == tagsStr || tagsStr.isEmpty()) {
            return;
        }
        for (String tagStr : tagsStr) {
            Tag tag = tagDAO.getById(tagStr);
            if (null == tag) {
                continue;
            }

            //关闭设备的校准状态
            tag.setCalibrationStatus(Constants.Calibrate.STATUS_OFF);

            List<TagCalibrationUpload> lowList = tagCalibrationUploadDAO.getByTagNoWithTimeLimit
                    (tagStr, calibration.getLowTempStartTime(), calibration.getMediumTempStartTime());
            List<TagCalibrationUpload> mediumList = tagCalibrationUploadDAO.getByTagNoWithTimeLimit
                    (tagStr, calibration.getMediumTempStartTime(), calibration.getHighTempStartTime());
            List<TagCalibrationUpload> highList = tagCalibrationUploadDAO.getByTagNoWithTimeLimit
                    (tagStr, calibration.getHighTempStartTime(), calibration.getEndTime());

            float lowErr, mediumErr, highErr, lowHErr, mediumHErr, highHErr;

            lowErr = getMinErr(lowList, calibration.getLowTemp(), true);
            lowHErr = getMinErr(lowList, calibration.getLowHumidity(), false);

            mediumErr = getMinErr(mediumList, calibration.getMediumTemp(), true);
            mediumHErr = getMinErr(mediumList, calibration.getMediumHumidity(), false);

            highErr = getMinErr(highList, calibration.getHighTemp(), true);
            highHErr = getMinErr(highList, calibration.getHighHumidity(), false);

            int count = 0;
            float precision = 0f, hprecision = 0f;
            if (lowErr != 0) {
                precision += lowErr;
                count++;
            }
            if (mediumErr != 0) {
                precision += mediumErr;
                count++;
            }
            if (highErr != 0) {
                precision += highErr;
                count++;
            }
            if (count == 0) {
                precision = 0f;
            } else {
                precision /= count;
            }

            count = 0;
            if (lowHErr != 0) {
                hprecision += lowHErr;
                count++;
            }
            if (mediumHErr != 0) {
                hprecision += mediumHErr;
                count++;
            }
            if (highHErr != 0) {
                hprecision += highHErr;
                count++;
            }
            if (count == 0) {
                hprecision = 0f;
            } else {
                hprecision /= count;
            }

            if (null != tag.getPrecision()) {
                precision = tag.getPrecision() - precision;
            } else {
                precision = 0 - precision;
            }
            precision = (float) (Math.round(precision * 100)) / 100;
            tag.setPrecision(precision);

            if (null != tag.gethPrecision()) {
                hprecision = tag.gethPrecision() - hprecision;
            } else {
                hprecision = 0 - hprecision;
            }
            hprecision = (float) (Math.round(hprecision * 100)) / 100;
            tag.sethPrecision(hprecision);

            if (lowErr != 0 || null != calibration.getLowTemp()) {
                tag.setStandardLowTemp(calibration.getLowTemp());
                tag.setCalibrationLowTemp(round2point(calibration.getLowTemp() - lowErr + precision));
            } else {
                tag.setStandardLowTemp(0);
                tag.setCalibrationLowTemp(0);
            }

            if (mediumErr != 0 || null != calibration.getMediumTemp()) {
                tag.setStandardMediumTemp(calibration.getMediumTemp());
                tag.setCalibrationMediumTemp(round2point(calibration.getMediumTemp() - mediumErr + precision));
            } else {
                tag.setStandardMediumTemp(0);
                tag.setCalibrationMediumTemp(0);
            }

            if (highErr != 0 || null != calibration.getHighTemp()) {
                tag.setStandardHighTemp(calibration.getLowTemp());
                tag.setCalibrationHighTemp(round2point(calibration.getLowTemp() - highErr + precision));
            } else {
                tag.setStandardHighTemp(0);
                tag.setCalibrationHighTemp(0);
            }

            if (lowHErr != 0 || null != calibration.getLowHumidity()) {
                tag.setStandardHumidity(calibration.getLowHumidity());
                tag.setCalibrationHumidity(round2point(calibration.getLowHumidity() - lowHErr + hprecision));
            } else if (mediumHErr != 0 || null != calibration.getMediumHumidity()) {
                tag.setStandardHumidity(calibration.getMediumHumidity());
                tag.setCalibrationHumidity(round2point(calibration.getMediumHumidity() - mediumHErr + hprecision));
            } else if (highHErr != 0 || null != calibration.getHighHumidity()) {
                tag.setStandardHumidity(calibration.getHighHumidity());
                tag.setCalibrationHumidity(round2point(calibration.getHighHumidity() - highHErr + hprecision));
            } else {
                tag.setStandardHumidity(0);
                tag.setCalibrationHumidity(0);
            }

            tagDAO.update(tag);

        }
    }

    private float round2point(float num) {
        return (float) (Math.round(num * 100)) / 100;
    }

    @Override
    public Map<String, Object> getTagCalibrationUpload(int start, int length, String tagNo, int id) {
        Map<String, Object> result = new HashMap<>(3);
        result.put("recordsTotal", 0);
        result.put("recordsFiltered", 0);
        result.put("data", Collections.<TagCalibrationUpload>emptyList());

        Calibration calibration = calibrationDAO.getById(id);
        if (null == calibration) {
            return result;
        }
        long count = tagCalibrationUploadDAO.countByTagNoWithTimeLimit(tagNo, calibration.getCreationTime(), calibration.getEndTime());
        result.put("recordsTotal", count);
        result.put("recordsFiltered", count);
        List<TagCalibrationUpload> data = tagCalibrationUploadDAO.getByTagNoWithTimeLimit(tagNo, calibration.getCreationTime(), calibration.getEndTime(), start, length);
        if (null != data) {
            result.put("data", data);
        }
        return result;
    }

    private float getMinErr(List<TagCalibrationUpload> list, Float value, boolean isTemp) {
        if (null == list || list.size() < 1 || value == null) {
            return 0f;
        }
        if (isTemp) {
            float err = 100f;
            for (TagCalibrationUpload tagCalibrationUpload : list) {
                float temp = tagCalibrationUpload.getTemperature() - value;
                if (Math.abs(temp) < Math.abs(err)) {
                    err = temp;
                }
            }
            if (err == 100f) {
                return 0f;
            }
            return err;
        } else {
            float err = 100f;
            for (TagCalibrationUpload tagCalibrationUpload : list) {
                float temp = tagCalibrationUpload.getHumidity() - value;
                if (Math.abs(temp) < Math.abs(err)) {
                    err = temp;
                }
            }
            if (err == 100f) {
                return 0f;
            }
            return err;
        }
    }
}
