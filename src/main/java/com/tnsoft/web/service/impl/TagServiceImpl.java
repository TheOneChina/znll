package com.tnsoft.web.service.impl;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.dao.*;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("tagService")
public class TagServiceImpl extends BaseServiceImpl<Tag> implements TagService {

    @Resource(name = "tagDAO")
    private TagDAO tagDAO;
    @Resource(name = "tagExpressDAO")
    private TagExpressDAO tagExpressDAO;
    @Resource(name = "expressDAO")
    private ExpressDAO expressDAO;
    @Resource(name = "userDAO")
    private UserDAO userDAO;
    @Resource(name = "userExpressDAO")
    private UserExpressDAO userExpressDAO;
    @Resource(name = "expressService")
    private ExpressService expressService;
    @Resource()
    private DomainDAO domainDAO;

    @Override
    public Response scanTag(String tagNo, int domainId) {

        Response res = new Response(Response.ERROR);
        Tag tag = getById(tagNo);
        if (null == tag) {
            res.setMessage("设备不存在！");
            return res;
        }
        if (tag.getStatus() == Constants.TagState.STATE_DELETE) {
            res.setMessage("设备不可用！");
            return res;
        }
        Domain domain = domainDAO.getById(domainId);
        if (null == domain) {
            res.setMessage("站点错误！");
            return res;
        }
        if (null != tag.getSoftwareType() && tag.getSoftwareType() != domain.getVersion()) {
            res.setMessage("设备类型不符！");
            return res;
        }
        if (null == tag.getDomainId()) {
            Date now = new Date();
            tag.setCreationTime(now);
            tag.setDomainId(domainId);
            tag.setLastModitied(now);
            if (null == tag.getStartUseTime()) {
                tag.setStartUseTime(now);
                tag.setCalibrationTime(now);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                calendar.add(Calendar.YEAR, 1);
                now = calendar.getTime();
                tag.setServiceExpirationTime(now);
            }
            update(tag);
            res.setCode(Response.OK);
            res.setMessage("成功！");
            return res;
        } else {
            if (tag.getDomainId().equals(domainId)) {
                res.setCode(Response.OK);
                res.setMessage("设备已添加，请勿重复添加！");
                return res;
            } else {
                res.setMessage("设备已被其他用户使用！");
                return res;
            }
        }
    }

    @Override
    public Response enableTag(String tagNo, int userId, Integer flag) {
        Response response = new Response(Response.ERROR);
        User user = userDAO.getById(userId);
        if (null == user) {
            response.setMessage("未找到用户！");
            return response;
        }
        //先添加设备
        response = scanTag(tagNo, user.getRootDomainId());
        if (response.getCode() == Response.ERROR) {
            return response;
        }

        Tag tag = tagDAO.getById(tagNo);
        if (null != tag) {
            List<Tag> tags = new ArrayList<>();
            tags.add(tag);
            response.setTags(tags);
        }

        List<TagExpress> tagExpressList = tagExpressDAO.getTagExpressByTagNoAndStatus(tagNo, Constants
                .TagExpressState.STATE_ACTIVE);
        if (null != tagExpressList && tagExpressList.size() >= 1) {
            List<Express> expresses = new ArrayList<>();
            for (TagExpress tagExpress : tagExpressList) {
                Express express = expressDAO.getById(tagExpress.getExpressId());
                if (null != express && express.getStatus() != Constants.ExpressState.STATE_FINISHED) {
                    UserExpress userExpress = userExpressDAO.getLastUserExpress(express.getId());
                    if (userDAO.getUserRole(userId).get(0).isDomainRights()) {
                        //若为站点权限
                        if (userExpress.getDomainId() == user.getDomainId()) {
                            expresses.add(express);
                        } else {
                            if (null == flag || flag > 1) {
                                //判断为非物流版再进行这一步
                                response.setCode(Response.ERROR);
                                response.setMessage("该设备正被其他站点使用！");
                                return response;
                            }
                        }
                    } else {
                        if (userExpress.getUserId() == userId) {
                            expresses.add(express);
                        } else {
                            if (null == flag || flag > 1) {
                                //判断为非物流版再进行这一步
                                response.setCode(Response.ERROR);
                                response.setMessage("该设备正被其他用户使用！");
                                return response;
                            }
                        }
                    }
                }
            }
            if (null != flag && flag == 1) {
                response.setCode(Response.OK);
                response.setExpress(expresses);
                return response;
            }
            if (expresses.size() <= 1) {
                response.setCode(Response.OK);
                response.setExpress(expresses);
            } else {
                response.setCode(Response.ERROR);
                response.setMessage("存在多个未结束的监测点！");
            }
        }
        return response;
    }

    @Override
    public User getCurrentUserByTagNo(String tagNo) {
        if (StringUtils.isEmpty(tagNo)) {
            return null;
        }
        List<TagExpress> tagExpressList = tagExpressDAO.getTagExpressByTagNoAndStatus(tagNo, Constants.TagExpressState.STATE_ACTIVE);
        if (null == tagExpressList || tagExpressList.isEmpty()) {
            return null;
        }
        int expressId = tagExpressList.get(tagExpressList.size() - 1).getExpressId();
        List<UserExpress> userExpressList = userExpressDAO.getUserExpressByEId(expressId);
        if (null == userExpressList || userExpressList.isEmpty()) {
            return null;
        }
        int userId = userExpressList.get(userExpressList.size() - 1).getUserId();
        return userDAO.getById(userId);
    }

    @Override
    public List<Express> getAllExpresses(String tagNo) {
        List<TagExpress> list = tagExpressDAO.getTagExpresses(tagNo);
        List<Express> list2 = new ArrayList<>();
        for (TagExpress te : list) {
            list2.add(expressDAO.getById(te.getExpressId()));
        }
        Collections.sort(list2, new Comparator<Express>() {
            @Override
            public int compare(Express o1, Express o2) {
                if (o1.getStatus() < o2.getStatus()) {
                    return -1;
                } else {
                    if (null != o1.getCheckOutTime() && null != o2.getCheckOutTime()) {
                        if (o1.getCheckOutTime().after(o2.getCheckOutTime())) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (o1.getCreationTime().after(o2.getCreationTime())) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }
            }
        });
        return list2;
    }

    @Override
    public Response saveTagAPConfig(String SSID, String password, String tagNo) {
        // 获得tag
        Response res = new Response();
        try {
            Tag tag = tagDAO.getById(tagNo);
            tag.setSSID(SSID);
            tag.setPassword(password);
            res.setCode(0);
            res.setMessage("设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.setCode(1);
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public List<Tag> getTagByUId(Integer userId) {
        // 虽然暂时只需要电量和设备编号,但是为了以后方便维护,将所有记录查询出来
        User user = userDAO.getById(userId);
        int domainId = user.getRootDomainId();
        // 使用Set来存放tag,放入已经存在的对象会被覆盖,
        List<Tag> tags = new ArrayList<>();
        tagDAO.getTagsByDomainId(domainId);
        return tags;
    }

    @Override
    public Tag getTagByEId(int expressId) {

        // 再根据获得的订单找到订单设备关系
        TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(expressId);
        if (null == tagExpress) {
            return null;
        }
        // 根据订单设备关系找到设备编号,根基设备编号找到设备
        return getById(tagExpress.getTagNo());
    }

    @Override
    public Response editTag(String[] tagNos, String SSID, String password, String lowTemp, String highTemp, String
            uploadCycle, Integer buzzer, Integer appointStart, String alertPhones, int flag) {
        Response res = new Response(Response.ERROR);
        try {
            for (String tagNo : tagNos) {
                Tag tag = getById(tagNo);
                if (tag != null) {
                    if (SSID != null && !SSID.equals("")) {
                        if (Utils.isValidAPName(SSID)) {
                            tag.setSSID(SSID);
                        }
                    }
                    if (null != password && !password.equals("")) {
                        if (Utils.isValidAPPasswd(password)) {
                            tag.setPassword(password);
                        }
                    }

                    Integer time = null;
                    if (!StringUtils.isEmpty(uploadCycle)) {
                        time = Integer.parseInt(uploadCycle);
                    }

                    Float min = null, max = null;
                    if (lowTemp != null && (!lowTemp.equals(""))) {
                        min = Float.parseFloat(lowTemp);
                    }
                    if (highTemp != null && (!highTemp.equals(""))) {
                        max = Float.parseFloat(highTemp);
                    }
                    if (null != max && null != min && max <= min) {
                        res.setMessage("温度输入不合法");
                    } else {
                        tag.setTemperatureMin(min);
                        tag.setTemperatureMax(max);
                        if (flag > 1) {
                            //医药版和标准版设置设备需同步至监测点
                            List<Express> expressList = expressService.getExpressesByTagNo(tag.getTagNo());
                            if (null != expressList && expressList.size() > 0) {
                                for (Express anExpressList : expressList) {
                                    if (anExpressList.getStatus() != Constants.ExpressState.STATE_FINISHED) {
                                        expressService.syncTagParam(anExpressList.getId(), min, max, time);
                                    }
                                }
                            }
                        }
                    }
                    if (null != time) {
                        tag.setSleepTime(time);
                        if (flag > 1) {
                            tag.setExpressSleepTime(time);
                        }
                    }
                    tag.setBuzzer(buzzer);
                    tag.setAppointStart(appointStart);
                    tag.setAlertPhones(alertPhones);
                }
            }
            res.setCode(Response.OK);
            res.setMessage("设置成功");
        } catch (Exception e) {
            res.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Response editBuzzer(String tagNo, int model) {
        Response res = new Response();
        try {
            Tag tag = tagDAO.getById(tagNo);
            tag.setBuzzer(model);
            res.setCode(0);
            res.setMessage(model == 0 ? "关闭蜂鸣器成功" : "开启蜂鸣器成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.setCode(1);
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @Override
    public Response tagTemplate(String[] tagNos, Integer model) {
        Response res = new Response();
        try {
            for (String tagNo : tagNos) {
                Tag tag = getById(tagNo);
                if (model == 1) {
                    // 设为移动
                    tag.setBuzzer(0);
                    tag.setLastModitied(new Date());
                    tag.setSleepTime(5);
                    res.setMessage("批量移动设置成功");
                } else {
                    // 设为固定
                    tag.setBuzzer(0);
                    tag.setLastModitied(new Date());
                    tag.setSleepTime(30);
                    res.setMessage("批量固定设置成功");
                }
            }
            res.setCode(0);
        } catch (Exception e) {
            res.setCode(1);
            res.setMessage("设置失败");
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Response editAppointStart(String tagNo, String time) {
        Response res = new Response();
        try {
            Tag tag = getById(tagNo);
            if (tag != null) {
                tag.setAppointStart(Integer.parseInt(time));
                res.setCode(0);
                res.setMessage("设置成功");
            }
        } catch (Exception e) {
            res.setCode(1);
            res.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

//    @Override
//    public Response makeTag(String tagNo, Integer domainId) {
//        Response res = new Response();
//        try {
//            Tag tag = new Tag();
//            tag.setTagNo(tagNo);
//            tag.setStatus(Constants.TagState.STATE_ACTIVE);
//            tag.setSSID("znll");// 新出厂的wifi名
//            tag.setPassword("88886666");// 新出厂的wifi密码
//            tag.setPrecision(0f);// 新出厂的误差为0
//            tag.setBuzzer(Constants.TagBuzzerState.STATE_OFF);// 新出厂的关闭蜂鸣器
//            tag.setSleepTime(0);// 新出厂的长睡眠
//            tag.setCreationTime(new Date());
//            tag.setDomainId(domainId);
//            tag.setLastModitied(new Date());
//            save(tag);
//            res.setCode(0);
//            res.setMessage("成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.setCode(1);
//            res.setMessage(e.getMessage());
//            throw new RuntimeException(e);
//        }
//        return res;
//    }

    @Override
    public Response editName(String tagNo, String name) {
        Response response = new Response(Response.ERROR);
        if (StringUtils.isEmpty(tagNo) || StringUtils.isEmpty(name)) {
            return response;
        }
        Tag tag = getById(tagNo);
        if (null != tag) {
            tag.setName(name);
            response.setCode(Response.OK);
            return response;
        }
        return response;
    }

    @Override
    public Response editAlertPhones(String tagNo, String alertPhones) {
        Response response = new Response(Response.ERROR);
        if (StringUtils.isEmpty(tagNo)) {
            return response;
        }
        Tag tag = getById(tagNo);
        if (null == tag) {
            return response;
        }

        if (null == alertPhones) {
            tag.setAlertPhones(null);
            response.setCode(Response.OK);
        } else {
            String[] phones = alertPhones.split(";");
            if (phones.length < 1) {
                tag.setAlertPhones(null);
                response.setCode(Response.OK);
            } else {
                for (String temp : phones) {
                    if (!Utils.isMobileNO(temp)) {
                        return response;
                    }
                }
                tag.setAlertPhones(alertPhones);
                response.setCode(Response.OK);
            }
        }
        return response;
    }

    @Override
    public Response editSleepTime(String tagNo, Integer minutes) {
        Response response = new Response(Response.ERROR);
        if (null == tagNo || null == minutes) {
            return response;
        }
        if (tagNo.isEmpty()) {
            return response;
        }
        if (minutes > 60 || minutes < 1) {
            return response;
        }
        Tag tag = getById(tagNo);
        if (null != tag) {
            tag.setSleepTime(minutes);
            if (null != tag.getDomainId()) {
                int version = domainDAO.getById(tag.getDomainId()).getVersion();
                if (version > Constants.Version.EXPRESS) {
                    //将设置同步至订单
                    tag.setExpressSleepTime(minutes);
                    List<Express> expresses = expressService.getExpressesByTagNo(tag.getTagNo());
                    if (null != expresses && expresses.size() > 0) {
                        for (Express express : expresses) {
                            if (express.getStatus() != Constants.ExpressState.STATE_FINISHED) {
                                expressDAO.saveExpressSleepTime(express.getId(), minutes);
                            }
                        }
                    }
                }
            }
            response.setCode(Response.OK);
            return response;
        }
        return response;
    }

    @Override
    public Response editTemperature(String tagNo, Float min, Float max) {
        Response response = new Response(Response.ERROR);
        response.setMessage("设置失败");
        if (null == tagNo) {
            return response;
        }
        if (tagNo.isEmpty()) {
            return response;
        }

        Tag tag = getById(tagNo);
        if (null != tag) {
            if (null != max && null != min) {
                if (max <= min) {
                    response.setMessage("输入下限应低于上限！");
                    return response;
                }
            }
            tag.setTemperatureMin(min);
            tag.setTemperatureMax(max);

            if (null != tag.getDomainId()) {
                int version = domainDAO.getById(tag.getDomainId()).getVersion();
                if (version > Constants.Version.EXPRESS) {
                    //将设置同步至订单
                    List<Express> expresses = expressService.getExpressesByTagNo(tag.getTagNo());
                    if (null != expresses && expresses.size() > 0) {
                        for (Express express : expresses) {
                            if (express.getStatus() != Constants.ExpressState.STATE_FINISHED) {
                                expressDAO.saveExpressTemperature(express.getId(), max, min);
                            }
                        }
                    }
                }
            }
            response.setCode(Response.OK);
            response.setMessage("设置成功");
            return response;
        }
        return response;
    }

    @Override
    public Tag getTagInfoByNoAndUserId(String tagNo, int userId) {
        Tag tag = getById(tagNo);
        User user = userDAO.getById(userId);
        if (null == tag || null == user) {
            return null;
        }
        if (tag.getDomainId().equals(user.getRootDomainId())) {
            return tag;
        }
        return null;
    }

    @Override
    public Response deleteTag(String tagNo, int userId) {
        Response response = new Response(Response.ERROR);
        if (StringUtils.isEmpty(tagNo) || userId < 1) {
            return response;
        }
        Tag tag = getById(tagNo);
        User user = userDAO.getById(userId);
        if (null == tag || null == user) {
            return response;
        }
        if (tag.getDomainId().equals(user.getRootDomainId())) {
            List<TagExpress> list = tagExpressDAO.getTagExpressByTagNoAndStatus(tagNo, Constants.TagExpressState
                    .STATE_ACTIVE);
            if (null != list && !list.isEmpty()) {
                Set<Integer> expressIdSet = new HashSet<>();
                for (TagExpress tagExpress : list) {
                    tagExpress.setStatus(Constants.TagExpressState.STATE_DELETE);
                    expressIdSet.add(tagExpress.getExpressId());
                }
                expressService.signExpressByIdList(expressIdSet);
            }
            tag.setDomainId(null);
            response.setCode(Response.OK);
            return response;
        }
        return response;
    }


    @Override
    public boolean createNewTags(int nums, int hardwareType) {
        if (nums < 1) {
            return false;
        }
        for (int i = 0; i < nums; i++) {
            Tag tag = new Tag();
            tag.setStatus(Constants.TagState.STATE_ACTIVE);
            tag.setTagNo(Utils.getUUID());
            tag.setSSIDNow("znll");// 新出厂的wifi名
            tag.setPasswordNow("88886666");// 新出厂的wifi密码
            tag.setCreationTime(new Date());
            tag.setLastModitied(new Date());
            tag.setSms(0);
            tag.setPrecision(0f);// 新出厂的误差为0
            tag.setBuzzer(Constants.TagBuzzerState.STATE_OFF);//新出厂的关闭蜂鸣器
            tag.setSleepTime(0);// 新出厂的长睡眠
            tag.setHardwareType(hardwareType);
            save(tag);
        }
        return true;
    }

    @Override
    public boolean saveCalibration(String tagNo, int calibrationType, float standardLowTemp, float lowTemp, float
            standardMediumTemp, float mediumTemp, float standardHighTemp, float highTemp, float standardHumidity,
                                   float humidity) {
        Tag tag = tagDAO.getById(tagNo);
        if (null == tag) {
            return false;
        }

        tag.setCalibrationType(calibrationType);
        tag.setStandardLowTemp(standardLowTemp);
        tag.setCalibrationLowTemp(lowTemp);
        tag.setStandardMediumTemp(standardMediumTemp);
        tag.setCalibrationMediumTemp(mediumTemp);
        tag.setStandardHighTemp(standardHighTemp);
        tag.setCalibrationHighTemp(highTemp);
        tag.setStandardHumidity(standardHumidity);
        tag.setCalibrationHumidity(humidity);

        tag.setCalibrationTime(new Date());
        return true;
    }

    @Override
    public Response addSMS(String[] tagNos, int num) {
        Response response = new Response(Response.ERROR);
        if (null == tagNos || tagNos.length < 1 || num <= 0) {
            response.setMessage("error param");
            return response;
        }
        StringBuilder message = new StringBuilder("为设备");
        for (String tagNo : tagNos) {
            if (tagNo.trim().isEmpty()) {
                continue;
            }
            Tag tag = tagDAO.getById(tagNo);
            if (null != tag) {
                tag.setSms(tag.getSms() + num);
            }
        }
        message.append("增加").append(num).append("条短信");
        response.setCode(Response.OK);
        response.setMessage(message.toString());
        return response;
    }

    @Override
    public Response addServiceTime(String[] tagNos, int year) {
        Response response = new Response(Response.ERROR);
        if (null == tagNos || tagNos.length < 1 || year <= 0) {
            response.setMessage("error param");
            return response;
        }
        StringBuilder message = new StringBuilder("为设备");
        for (String tagNo : tagNos) {
            if (tagNo.trim().isEmpty()) {
                continue;
            }
            Tag tag = tagDAO.getById(tagNo);
            if (null != tag) {
                Date now = new Date();
                Calendar calendar = Calendar.getInstance();
                if (null == tag.getServiceExpirationTime() || tag.getServiceExpirationTime().before(now)) {
                    calendar.setTime(now);
                } else {
                    calendar.setTime(tag.getServiceExpirationTime());
                }
                calendar.add(Calendar.YEAR, year);
                tag.setServiceExpirationTime(calendar.getTime());
            }
        }
        message.append("增加").append(year).append("年平台使用期");
        response.setCode(Response.OK);
        response.setMessage(message.toString());
        return response;
    }

}
