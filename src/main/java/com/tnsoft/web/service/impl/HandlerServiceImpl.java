package com.tnsoft.web.service.impl;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.dao.*;
import com.tnsoft.web.model.AuthResponse;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.UploadResponse;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.HandlerService;
import com.tnsoft.web.service.SendAlertSMSService;
import com.tnsoft.web.util.Utils;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.ApplicationContextWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.applet.Main;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Transactional
@Service("handlerService")
public class HandlerServiceImpl implements HandlerService {

    /**
     * 短信报警间隔
     */
    private static final long SMS_ALERT_CYCLE = 3600000L;
    @Resource(name = "expressService")
    private ExpressService expressService;
    @Resource(name = "tagDAO")
    private TagDAO tagDAO;
    @Resource(name = "tempExpressDAO")
    private TempExpressDAO tempExpressDAO;
    @Resource(name = "alertLevelDAO")
    private AlertLevelDAO alertLevelDAO;
    @Resource(name = "alertDAO")
    private AlertDAO alertDAO;
    @Resource(name = "tagExpressDAO")
    private TagExpressDAO tagExpressDAO;
    @Resource(name = "sendAlertSMSService")
    private SendAlertSMSService sendAlertSMSService;
    @Resource(name = "tagCalibrationUploadDAO")
    private TagCalibrationUploadDAO tagCalibrationUploadDAO;
    @Resource()
    private UserExpressDAO userExpressDAO;

    /**
     * 针对硬件模块只能读取固定4位整数加1位符号位，将float数据进行*100后转成固定位数的String.
     */
    private static String transFromFloat(float in) {
        if (in >= 100 || in <= -100) {
            return "-0000";
        }
        StringBuilder sBuilder = new StringBuilder();
        if (in >= 0) {
            sBuilder.append("0");
        } else {
            sBuilder.append("-");
            in = 0 - in;
        }
        int temp = (int) (in * 100);
        String tempS = temp + "";
        int size = tempS.length();
        for (; size < 4; size++) {
            sBuilder.append("0");
        }
        sBuilder.append(tempS);
        return sBuilder.toString();
    }

    @Override
    public void handleData(String tagNo, Float temperature, Float humidity, Date dataTime, Integer Vdd, Integer
            WIFI, Integer FlashErr) {
        if (StringUtils.isEmpty(tagNo) || null == temperature || null == humidity) {
            return;
        }
        boolean isOnlineData = false;
        if (null == dataTime) {
            isOnlineData = true;
        }
        Tag tag = tagDAO.getById(tagNo);
        if (null == tag) {
            return;
        }
        if (isOnlineData && tag.getStatus() == Constants.TagState.STATE_DELETE) {
            return;
        }
        //更新最后通信时间
        tag.setLastConnected(new Date());

        if (null != Vdd) {
            tag.setElectricity(Vdd);
        }
        if (null != WIFI) {
            tag.setWifiStatus(WIFI);
        }
        //是否电量过低
        boolean isLowPower = false;
        if (null != Vdd && Vdd != 1 && Vdd < Constants.Electricity.NORMAL) {
            isLowPower = true;
        }
        if (null != FlashErr && FlashErr == 1) {
            isLowPower = true;
        }
        if (isLowPower) {
            List<Express> expressesForVdd = expressService.getExpressesByTagNo(tagNo);
            if (null != expressesForVdd && expressesForVdd.size() > 0) {
                for (Express express : expressesForVdd) {
                    //低电量报警
                    NDAAlert eAlert = new NDAAlert();
                    eAlert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
                    eAlert.setCreationTime(new Date());
                    eAlert.setAlertTime(new Date());
                    UserExpress userExpress = userExpressDAO.getLastUserExpress(express.getId());
                    if (null != userExpress) {
                        eAlert.setDomainId(userExpress.getDomainId());
                    } else {
                        eAlert.setDomainId(tag.getDomainId());
                    }
                    eAlert.setType(Constants.AlertType.STATE_ELECTRICITY);
                    eAlert.setLastModitied(new Date());
                    eAlert.setTagNo(tag.getTagNo());
                    eAlert.setExpressId(express.getId());
                    eAlert.setStatus(Constants.AlertState.STATE_ACTIVE);
                    alertDAO.save(eAlert);
                    express.addAlertCount();
                }
            }
        }

        //温度数据校正
        if (null != tag.getPrecision()) {
            temperature += tag.getPrecision();
        }

        //湿度数据校正
        if (null != tag.gethPrecision()) {
            humidity += tag.gethPrecision();
        }
        if (humidity == 0) {
            humidity = 0f;
        } else if (humidity > 99.00) {
            humidity = 99.00f;
        } else if (humidity < 1.00) {
            humidity = 1.00f;
        }

        //如果设备处于校准状态,则执行校准流程后退出
        if (tag.getCalibrationStatus() == Constants.Calibrate.STATUS_ON) {
            TagCalibrationUpload upload = new TagCalibrationUpload();
            upload.setTagNo(tagNo);
            upload.setTemperature(temperature);
            upload.setHumidity(humidity);
            if (isOnlineData) {
                upload.setTime(new Date());
            } else {
                upload.setTime(dataTime);
            }
            tagCalibrationUploadDAO.save(upload);
            return;
        }

        List<Express> expresses = expressService.getExpressesByTagNo(tagNo);

        if (null == expresses || expresses.isEmpty()) {
            return;
        }

        //短信报警部分
        if (null != tag.getAlertPhones()) {
            String phones = tag.getAlertPhones();
            if (!phones.isEmpty()) {
                String[] phonesArray = phones.split(";");
                Float expressTMin = null;
                Float expressTMax = null;
                if (null != tag.getExpressTMin()) {
                    expressTMin = tag.getExpressTMin();
                } else if (null != tag.getTemperatureMin()) {
                    expressTMin = tag.getTemperatureMin();
                }
                if (null != tag.getExpressTMax()) {
                    expressTMax = tag.getExpressTMax();
                } else if (null != tag.getTemperatureMax()) {
                    expressTMax = tag.getTemperatureMax();
                }
                int type = 0;
                if (null != expressTMin && temperature < expressTMin) {
                    type = Constants.SMSAlertType.TYPE_TEMP_LOW;
                }
                if (null != expressTMax && temperature > expressTMax) {
                    type = Constants.SMSAlertType.TYPE_TEMP_HIGH;
                }
                if (isLowPower) {
                    type = Constants.SMSAlertType.TYPE_TEMP_ELECTRICITY;
                }
                if (type > 0) {
                    Date lastSMSAlert = tag.getLastSMSAlert();
                    Date dateTemp = new Date();
                    dateTemp.setTime(dateTemp.getTime() - SMS_ALERT_CYCLE);
                    if (null == lastSMSAlert || lastSMSAlert.before(dateTemp)) {
                        boolean flag = false;
                        String context = tagNo;
                        if (!StringUtils.isEmpty(tag.getName())) {
                            context = tag.getName();
                        }
                        for (String mobile : phonesArray) {
                            if (Utils.isMobileNO(mobile) && tag.getSms() > 0) {
                                boolean result = sendAlertSMSService.sendAlertSMS(context, mobile, type, tag
                                        .getDomainId());
                                if (result) {
                                    flag = true;
                                    tag.setSms(tag.getSms() - 1);
                                }
                            }
                        }
                        if (flag) {
                            tag.setLastSMSAlert(new Date());
                        }
                    }
                }
            }
        }

        Date now = new Date();

        if (expresses.size() == 1) {
            //判断只有一个订单时，需要执行设备的延时启动
            if (null != tag.getAppointStart() && tag.getAppointStart() > 0) {
                if (!tempExpressDAO.hasData(expresses.get(0).getId())) {
                    Date creationTime = expresses.get(0).getCreationTime();
                    int time = (int) ((now.getTime() - creationTime.getTime()) / 60000);
                    if (time < tag.getAppointStart()) {
                        return;
                    }
                }
            }
        }

        for (Express ex : expresses) {
            if (isOnlineData) {
                if (null == ex.getLastDataTime()) {
                    handleDataByExpress(ex, now, tag, temperature, humidity);
                } else {
                    int sleepTime;
                    if (null != ex.getSleepTime()) {
                        sleepTime = ex.getSleepTime();
                    } else if (null != tag.getExpressSleepTime()) {
                        sleepTime = tag.getExpressSleepTime();
                    } else if (null != tag.getSleepTime()) {
                        sleepTime = tag.getSleepTime();
                    } else {
                        sleepTime = 10;
                    }
                    Date timeTemp = new Date();
                    timeTemp.setTime(ex.getLastDataTime().getTime() + sleepTime * 60000);
                    if (now.before(timeTemp)) {
                        handleDataByExpress(ex, now, tag, temperature, humidity);
                    } else {
                        //数据缺失超过两个周期时不再补数据，只记录当前上传的一次数据
                        timeTemp.setTime(timeTemp.getTime() + sleepTime * 90000);
                        if (now.after(timeTemp)) {
                            //缺失数据超过两组，则不再补充，直接记录当前时间数据
                            handleDataByExpress(ex, now, tag, temperature, humidity);
                        } else {
                            //数据缺失不超过两个周期时，自动补充数据，最多补充两组数据
                            timeTemp.setTime(ex.getLastDataTime().getTime() + sleepTime * 60000);
                            while (now.after(timeTemp)) {
                                handleDataByExpress(ex, new Date(timeTemp.getTime()), tag, temperature, humidity);
                                timeTemp.setTime(timeTemp.getTime() + sleepTime * 60000);
                            }
                        }
                    }
                }
            } else {
                handleDataByExpress(ex, dataTime, tag, temperature, humidity);
            }

        }
    }


    private void handleDataByExpress(Express ex, Date time, Tag tag, float temperature, float humidity) {
        Calendar exTime = Calendar.getInstance();
        exTime.setTime(ex.getCreationTime());
        Calendar dataCalendar = Calendar.getInstance();
        dataCalendar.setTime(time);

        // 订单的预约启动时间
        Integer appointStart = null;
        if (ex.getAppointStart() != null) {
            appointStart = ex.getAppointStart();
        }
        if (appointStart != null) {
            exTime.add(Calendar.MINUTE, appointStart);
        }

        if (exTime.after(dataCalendar)) {
            return;
        }

        // 订单的预约结束时间
        if (ex.getAppointEnd() != null) {
            exTime.setTime(ex.getCreationTime());
            // 订单创建时间加上预约结束时间
            exTime.add(Calendar.MINUTE, ex.getAppointEnd());
            if (exTime.before(dataCalendar)) {
                return;
            }
        }

        // 不再保存签收后时间的温度
        if (ex.getCheckOutTime() != null) {
            exTime.setTime(ex.getCheckOutTime());
            if (exTime.before(dataCalendar)) {
                return;
            }
        }
        Date now = new Date();

        TempExpress a = new TempExpress();
        a.setCreationTime(time);
        a.setDomainId(tag.getDomainId());
        a.setExpressId(ex.getId());
        a.setLastModitied(now);
        a.setTemperature(temperature);
        a.setHumidity(humidity);

        tempExpressDAO.save(a);

        ex.setLastDataTime(time);

        UserExpress lastUserExpress = userExpressDAO.getLastUserExpress(ex.getId());

        // 开始报警逻辑
        Float max = null;
        Float min = null;
        // 记录温度之后,判断是否超过范围相当于是否触发报警
        if (ex.getTemperatureMax() != null) {
            max = ex.getTemperatureMax();
        } else if (tag.getTemperatureMax() != null) {
            max = tag.getTemperatureMax();
        }
        if (ex.getTemperatureMin() != null) {
            min = ex.getTemperatureMin();
        } else if (tag.getTemperatureMin() != null) {
            min = tag.getTemperatureMin();
        }

        boolean isAlert = (min != null && temperature < min) || (max != null && temperature > max);
        if (isAlert) {

            Logger.error("订单：" + ex.getExpressNo() + "触发报警。温度：" + temperature + "，低温设置：" + min + "，高温设置：" + max);

            // 生成普通报警信息
            NDAAlert alert = new NDAAlert();
            if (min != null && temperature < min) {
                alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML_LOW);
            } else {
                alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML_HIGH);
            }
            alert.setAlertTime(time);
            alert.setCreationTime(now);
            if (null != lastUserExpress) {
                alert.setDomainId(lastUserExpress.getDomainId());
            } else {
                alert.setDomainId(tag.getDomainId());
            }
            alert.setLastModitied(now);
            alert.setType(Constants.AlertType.STATE_TEMPHISALERT);
            alert.setExpressId(ex.getId());
            alert.setTagNo(tag.getTagNo());
            alert.setStatus(Constants.AlertState.STATE_ACTIVE);
            alertDAO.save(alert);
            ex.addAlertCount();

            //严重报警
            AlertLevel level = alertLevelDAO.getAlertLevel(tag.getDomainId(), Constants.AlertLevelType
                    .TEMP_SERIOUS);

            if (level != null && level.getStatus() == Constants.IsAble.ABLE) {
                int count = alertDAO.countAlertsByTime(ex.getId(), level.getHours());

                if (count >= level.getTimes()) {
                    NDAAlert sAlert = new NDAAlert();
                    sAlert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
                    sAlert.setCreationTime(now);
                    sAlert.setAlertTime(time);
                    if (null != lastUserExpress) {
                        sAlert.setDomainId(lastUserExpress.getDomainId());
                    } else {
                        sAlert.setDomainId(tag.getDomainId());
                    }
                    sAlert.setType(Constants.AlertType.STATE_TEMPHISALERT);
                    sAlert.setLastModitied(now);
                    sAlert.setTagNo(tag.getTagNo());
                    sAlert.setExpressId(ex.getId());
                    sAlert.setStatus(Constants.AlertState.STATE_ACTIVE);
                    alertDAO.save(sAlert);
                    ex.addAlertCount();
                }
            }

        }
        expressService.update(ex);
    }

    @Override
    public UploadResponse handleTagOnlineData(String tagNo, String temperature, Float humidity, Integer FlashErr) {

        if (null == tagNo || null == temperature || null == humidity) {
            return null;
        }

        UploadResponse up = new UploadResponse();
        UploadResponse.Datapoint d = up.new Datapoint();
        d.setCreated(Utils.SF1.format(new Date()));
        d.setX(temperature);
        d.setY(humidity + "");
        if (null != FlashErr) {
            d.setFlashErr(FlashErr + "");
        }
        up.setDatapoint(d);

        Tag tag = tagDAO.getById(tagNo);

        //判断设备是否属于系统及状态是否可用
        if (null == tag || tag.getStatus() == Constants.TagState.STATE_DELETE) {
            d.setDstime("0");
            d.setTagIsBind("0");
            d.setChange("false");
            up.setStatus(200);
            return up;
        }

        if (null != tag.getExpressSleepTime() && tag.getExpressSleepTime() > 0) {
            d.setDstime(tag.getExpressSleepTime() + "");
        } else if (null != tag.getSleepTime()) {
            d.setDstime(tag.getSleepTime() + "");
        }

        // 检测到设备未绑定订单时将设备上传周期置为0，发送睡眠时间0给设备。
        List<Express> expresses = expressService.getExpressesByTagNo(tagNo);

        //将为解绑的已完成订单与设备解绑
        if (expresses != null && expresses.size() > 0) {
            for (Express express : expresses) {
                if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
                    TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(express.getId());
                    if (null != tagExpress) {
                        tagExpress.setStatus(Constants.TagExpressState.STATE_DELETE);
                    }
                }
            }
        }

        //再次获取设备绑定的订单
        expresses = expressService.getExpressesByTagNo(tagNo);
        if (null == expresses || expresses.size() == 0) {
            tag.setStatus(Constants.TagState.STATE_ACTIVE);
            tag.setExpressSleepTime(0);
            tag.setExpressTMin(null);
            tag.setExpressTMax(null);
            tag.setExpressHMax(null);
            tag.setExpressHMin(null);
            d.setDstime(0 + "");
            d.setTagIsBind("0");
        } else {

            if (expresses.size() == 1) {
                //判断只有一个订单时，需要执行设备的延时启动
                if (null != tag.getAppointStart() && tag.getAppointStart() > 0) {
                    if (!tempExpressDAO.hasData(expresses.get(0).getId())) {
                        Date creationTime = expresses.get(0).getCreationTime();
                        Date now = new Date();
                        int time = (int) ((now.getTime() - creationTime.getTime()) / 60000);
                        if (time < tag.getAppointStart()) {
                            d.setDstime(tag.getAppointStart() - time + 1 + "");
                        }
                    }
                }
            }

            d.setTagIsBind("1");
        }

        //判断是否处于校准状态
        if (tag.getCalibrationStatus() == Constants.Calibrate.STATUS_ON) {
            d.setDstime(Constants.Calibrate.SLEEP_TIME + "");
        }

        // 检查设置参数是否需要发送
        boolean change = false;
        // 蜂鸣器
        if (null != tag.getBuzzer()) {
            if (!tag.getBuzzer().equals(tag.getBuzzerNow())) {
                change = true;
            }
        }
        // AP名和密码
//        if (null != tag.getSSID()) {
//            if (!tag.getSSID().equals(tag.getSSIDNow())) {
//                change = true;
//            }
//        }
//        if (null != tag.getPassword()) {
//            if (!tag.getPassword().equals(tag.getPasswordNow())) {
//                change = true;
//            }
//        }

        // 温度上下限
        if (null != tag.getExpressTMax()) {
            if (!tag.getExpressTMax().equals(tag.getTemperatureMaxNow())) {
                change = true;
            }
        } else if (null != tag.getTemperatureMax()) {
            if (!tag.getTemperatureMax().equals(tag.getTemperatureMaxNow())) {
                change = true;
            }
        } else {
            if (null != tag.getTemperatureMaxNow()) {
                change = true;
            }
        }


        if (null != tag.getExpressTMin()) {
            if (!tag.getExpressTMin().equals(tag.getTemperatureMinNow())) {
                change = true;
            }
        } else if (null != tag.getTemperatureMin()) {
            if (!tag.getTemperatureMin().equals(tag.getTemperatureMinNow())) {
                change = true;
            }
        } else {
            if (null != tag.getTemperatureMinNow()) {
                change = true;
            }
        }


        // 湿度上下限
//        if (null != tag.getExpressHMax()) {
//            if (!tag.getExpressHMax().equals(tag.getHumidityMaxNow())) {
//                change = true;
//            }
//        } else {
//            if (null != tag.getHumidityMax()) {
//                if (!tag.getHumidityMax().equals(tag.getHumidityMaxNow())) {
//                    change = true;
//                }
//            }
//        }
//
//        if (null != tag.getExpressHMin()) {
//            if (!tag.getExpressHMin().equals(tag.getHumidityMinNow())) {
//                change = true;
//            }
//        } else {
//            if (null != tag.getHumidityMin()) {
//                if (!tag.getHumidityMin().equals(tag.getHumidityMinNow())) {
//                    change = true;
//                }
//            }
//        }


        //校准值
        if (null != tag.getPrecision()) {
            if (!tag.getPrecision().equals(tag.getPrecisionNow())) {
                change = true;
            }
        }

        //全部设置都需要添加到报文中
        if (change) {
            d.setChange("true");
            d.setBuzzer(tag.getBuzzer() + "");
//            d.setSsid(tag.getSSID());
//            d.setPassword(tag.getPassword());

            //先判断订单温度设置，再判断设备温度设置
            if (null != tag.getExpressTMax()) {
                d.setTmax(transFromFloat(tag.getExpressTMax()));
            } else if (null != tag.getTemperatureMax()) {
                d.setTmax(transFromFloat(tag.getTemperatureMax()));
            } else {
                d.setTmax("09999");
            }

            if (null != tag.getExpressTMin()) {
                d.setTmin(transFromFloat(tag.getExpressTMin()));
            } else if (null != tag.getTemperatureMin()) {
                d.setTmin(transFromFloat(tag.getTemperatureMin()));
            } else {
                d.setTmin("-9999");
            }

            //温湿度上下限设置，同温度
            if (null != tag.getExpressHMax()) {
                d.setHmax(transFromFloat(tag.getExpressHMax()));
            } else if (null != tag.getHumidityMax()) {
                d.setHmax(transFromFloat(tag.getHumidityMax()));
            } else {
                d.setHmax("09999");
            }

            if (null != tag.getExpressHMin()) {
                d.setHmin(transFromFloat(tag.getExpressTMin()));
            } else if (null != tag.getHumidityMin()) {
                d.setHmin(transFromFloat(tag.getHumidityMin()));
            } else {
                d.setHmin("00000");
            }


            if (null == tag.getPrecision()) {
                d.setTemPrecision("00000");
            } else {
                d.setTemPrecision(transFromFloat(tag.getPrecision()));
            }

            if (null == tag.gethPrecision()) {
                d.setHumPrecision("00000");
            } else {
                d.setHumPrecision(transFromFloat(tag.gethPrecision()));
            }
        } else {
            d.setChange("false");
        }

        up.setStatus(200);

        return up;
    }

    @Override
    public void handleFeedbackData(String tagNo, String ssid, String password, Integer buzzer, Float precision, Float
            hPrecision, Float tmax, Float tmin, Float hmax, Float hmin) {
        if (null == tagNo) {
            return;
        }
        Tag tag = tagDAO.getById(tagNo);
        if (!StringUtils.isEmpty(ssid)) {
            tag.setSSIDNow(ssid);
        }
        if (!StringUtils.isEmpty(password)) {
            tag.setPasswordNow(password);
        }
        if (null != buzzer) {
            tag.setBuzzerNow(buzzer);
        }
        if (null != precision) {
            tag.setPrecisionNow(precision);
        }
        if (null != hPrecision) {
            tag.sethPrecisionNow(hPrecision);
        }

        tag.setTemperatureMaxNow(tmax);


        tag.setTemperatureMinNow(tmin);


        tag.setHumidityMaxNow(hmax);


        tag.setHumidityMinNow(hmin);

    }

    @Override
    public AuthResponse handleActiveData(String tagNo, Integer nonce) {
        if (StringUtils.isEmpty(tagNo)) {
            return null;
        }
        Tag tag = tagDAO.getById(tagNo);
        if (null == tag) {
            return null;
        }
        //更新最后通信时间
        tag.setLastConnected(new Date());

        String date = Utils.SF1.format(new Date());
        // 设置激活指令返回的数据
        AuthResponse au = new AuthResponse();
        AuthResponse.Device device = new AuthResponse.Device();
        device.setActivate_status(1);
        if (null != tag.getCreationTime()) {
            device.setActivated_at(Utils.SF1.format(tag.getCreationTime()));
            device.setCreated(Utils.SF1.format(tag.getCreationTime()));
        } else {
            device.setActivated_at(date);
            device.setCreated(date);
        }
        device.setBSSID(tag.getBSSID());
        device.setDescription("device-description-" + tag.getName());
        device.setId(1);
        device.setIs_frozen(0);
        device.setIs_private(1);
        device.setKey_id(1);
        if (null != tag.getLastModitied()) {
            device.setLast_active(Utils.SF1.format(tag.getLastModitied()));
            device.setLast_pull(Utils.SF1.format(tag.getLastModitied()));
        } else {
            device.setLast_active(date);
            device.setLast_pull(date);
        }

        device.setLocation("");
        device.setMetadata(tag.getBSSID() + "temperature");
        device.setName("device-name-" + tag.getName());
        device.setProduct_id(1);
        device.setProductbatch_id(1);
        device.setPtype(12335);
        device.setSerial(tag.getName());
        device.setStatus(2);
        device.setUpdated(date);
        device.setVisibly(1);
        au.setStatus(200);
        au.setMessage("device identified");
        au.setNonce(nonce);
        au.setDevice(device);

        return au;
    }

}
