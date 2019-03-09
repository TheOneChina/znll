package com.web.service.impl;

import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.hibernate.model.TagExpress;
import com.tnsoft.hibernate.model.TempExpress;
import com.tnsoft.web.dao.ExpressDAO;
import com.tnsoft.web.dao.TagDAO;
import com.tnsoft.web.dao.TagExpressDAO;
import com.tnsoft.web.dao.TempExpressDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.service.TempExpressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("tempExpressService")
public class TempExpressServiceImpl implements TempExpressService {

    public static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource(name = "tempExpressDAO")
    private TempExpressDAO tempExpressDAO;
    @Resource(name = "expressDAO")
    private ExpressDAO expressDAO;
    @Resource(name = "tagDAO")
    private TagDAO tagDAO;
    @Resource(name = "tagExpressDAO")
    private TagExpressDAO tagExpressDAO;

    @Override
    public Result queryByExpressId(Integer expressId) {

        Result result = new Result();
        result.setCode(Result.ERROR);
        List<String> time = new ArrayList<>();
        List<String> temperature = new ArrayList<>();
        List<String> humidity = new ArrayList<>();

        List<TempExpress> list = tempExpressDAO.getByExpressId(expressId);
        if (null != list && !list.isEmpty()) {
            for (TempExpress ne : list) {
                time.add(SF.format(ne.getCreationTime()));
                temperature.add(String.format("%.2f", ne.getTemperature()));
                humidity.add(String.format("%.2f", ne.getHumidity()));
            }
            result.setTime(time);
            result.setTemperature(temperature);
            result.setHumidity(humidity);
            result.setCode(Result.OK);
        }
        return result;
    }

    @Override
    public Response getByExpressIdWithDateLimit(int expressId, Date start, Date end) {
        Response response = new Response(Response.ERROR);
        if (expressId < 0) {
            return response;
        }
        List<TempExpress> list = tempExpressDAO.getByExpressIdWithTimeLimit(expressId, start,
                end);
        response.setTemps(list);
        response.setCode(Response.OK);
        return response;
    }

    @Override
    public Map<String, String> getBriefInfoByExpressId(Integer expressId) {
        Map<String, String> result = new HashMap<>();
        if (null == expressId) {
            return result;
        }

        Express express = expressDAO.getById(expressId);
        if (null == express) {
            return result;
        }

        TagExpress tagExpress = tagExpressDAO.getLastTagExpressByEId(expressId);

        List<TempExpress> ndaTempExpresses = tempExpressDAO.getByExpressId(expressId);

        Tag tag = null;
        float realMaxTemp = 0;
        float realMinTemp = 0;
        float realAveTemp = 0;
        float realMaxHumidity = 0;
        float realMinHumidity = 0;
        float realAveHumidity = 0;
        if (ndaTempExpresses.size() > 0) {

            realMaxTemp = ndaTempExpresses.get(0).getTemperature();
            realMinTemp = ndaTempExpresses.get(0).getTemperature();

            realMaxHumidity = ndaTempExpresses.get(0).getHumidity();
            realMinHumidity = ndaTempExpresses.get(0).getHumidity();

            for (TempExpress ndaTempExpress : ndaTempExpresses) {
                if (realMaxTemp < ndaTempExpress.getTemperature()) {
                    realMaxTemp = ndaTempExpress.getTemperature();
                }
                if (realMinTemp > ndaTempExpress.getTemperature()) {
                    realMinTemp = ndaTempExpress.getTemperature();
                }
                if (realMaxHumidity < ndaTempExpress.getHumidity()) {
                    realMaxHumidity = ndaTempExpress.getHumidity();
                }
                if (realMinHumidity > ndaTempExpress.getHumidity()) {
                    realMinHumidity = ndaTempExpress.getHumidity();
                }
                realAveTemp += (ndaTempExpress.getTemperature() / ndaTempExpresses.size());
                realAveHumidity += (ndaTempExpress.getHumidity() / ndaTempExpresses.size());
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (tagExpress == null) {
            result.put("tag", "当前未绑定设备");
            result.put("tagName", "当前未绑定设备");
        } else {
            result.put("tag", tagExpress.getTagNo());
            tag = tagDAO.getById(tagExpress.getTagNo());
            if (null != tag && null != tag.getName()) {
                result.put("tagName", tag.getName());
            } else {
                result.put("tagName", "");
            }
        }

        if (null != tag) {
            TempExpress newsetOneByExpressId = tempExpressDAO.getNewsetOneByExpressId(expressId);
            if (null == newsetOneByExpressId) {
                result.put("tagStatus", "离线");
            } else {

                Integer sleepTime;
                if (null != express.getSleepTime()) {
                    sleepTime = express.getSleepTime();
                } else if (null != tag.getExpressSleepTime()) {
                    sleepTime = tag.getExpressSleepTime();
                } else {
                    sleepTime = tag.getSleepTime();
                }
                if (null != sleepTime) {
                    Date now = new Date();
                    if (null != express.getCheckOutTime()) {
                        now.setTime(express.getCheckOutTime().getTime());
                    }
                    now.setTime(now.getTime() - sleepTime * 90000L);
                    if (newsetOneByExpressId.getCreationTime().after(now)) {
                        result.put("tagStatus", "在线");
                    } else {
                        result.put("tagStatus", "离线");
                    }
                } else {
                    result.put("tagStatus", "离线");
                }
            }
        }

        //所有报警
        result.put("alertCount", express.getAlertCount() + express.getHistoryAlertCount() + "");
        //未处理报警
        result.put("activeAlertCount", express.getAlertCount() + "");

        if (null == tag || null == tag.getElectricity()) {
            result.put("electricityStatus", "");
            result.put("electricity", "");
        } else {
            result.put("electricity", tag.getElectricity() + "");
            if (tag.getElectricity() >= Constants.Electricity.NORMAL || tag.getElectricity() == 1) {
                result.put("electricityStatus", "<font color='green'>充足</font>");
            } else {
                result.put("electricityStatus", "<font color='red'>不足</font>");
            }
        }

        if (express.getCreationTime() != null) {
            result.put("expressStartTime", dateFormat.format(express.getCreationTime()));
        } else {
            result.put("expressStartTime", "无");
        }

        if (express.getCheckOutTime() != null) {
            result.put("expressEndTime", dateFormat.format(express.getCheckOutTime()));
        } else {
            result.put("expressEndTime", "无");
        }

        if (express.getStatus() <= Constants.ExpressState.STATE_ACTIVE) {
            result.put("expressState", "配送中");
            result.put("monitorState", "监测中");
        } else if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
            result.put("expressState", "已签收");
            result.put("monitorState", "监测结束");
        } else {
            result.put("expressState", "未知");
            result.put("monitorState", "未知");
        }

        if (express.getTemperatureMax() != null) {
            result.put("maxAlertTemperature", express.getTemperatureMax() + "");
        } else {
            if (null != tag && tag.getTemperatureMax() != null) {
                result.put("maxAlertTemperature", tag.getTemperatureMax() + "");
            } else {
                result.put("maxAlertTemperature", "无");
            }
        }

        if (express.getTemperatureMin() != null) {
            result.put("minAlertTemperature", express.getTemperatureMin() + "");
        } else {
            if (null != tag && tag.getTemperatureMin() != null) {
                result.put("minAlertTemperature", tag.getTemperatureMin() + "");
            } else {
                result.put("minAlertTemperature", "无");
            }
        }

        result.put("realMaxTemp", String.format("%.2f℃", realMaxTemp));
        result.put("realMinTemp", String.format("%.2f℃", realMinTemp));
        result.put("realAveTemp", String.format("%.2f℃", realAveTemp));

        result.put("realMaxHumidity", String.format("%.2f", realMaxHumidity) + "%");
        result.put("realMinHumidity", String.format("%.2f", realMinHumidity) + "%");
        result.put("realAveHumidity", String.format("%.2f", realAveHumidity) + "%");

        result.put("expressNo", express.getExpressNo());

        if (ndaTempExpresses.size() > 0) {
            if (ndaTempExpresses.get(ndaTempExpresses.size() - 1).getCreationTime() != null) {
                result.put("nowTime",
                        dateFormat.format(ndaTempExpresses.get(ndaTempExpresses.size() - 1).getCreationTime())
                                + "");
                result.put("nowTemp", String.format("%.2f℃", ndaTempExpresses.get(ndaTempExpresses.size() - 1)
                        .getTemperature()));
                result.put("nowHumidity", String.format("%.2f", ndaTempExpresses.get(ndaTempExpresses.size() - 1)
                        .getHumidity()) + "%");
            }
        } else {
            result.put("nowTime", "");
            result.put("nowTemp", "");
            result.put("nowHumidity", "");
        }

        if (express.getDescription() != null) {
            result.put("expressDescription", express.getDescription());
        } else {
            result.put("expressDescription", "");
        }
        return result;
    }

}
