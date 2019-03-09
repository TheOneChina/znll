package com.web.service.impl;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.dao.*;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.UserExpressService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

@Transactional
@Service("expressService")
public class ExpressServiceImpl extends BaseServiceImpl<Express> implements ExpressService {

    @Resource(name = "expressDAO")
    private ExpressDAO expressDAO;
    @Resource(name = "userExpressDAO")
    private UserExpressDAO userExpressDAO;
    @Resource(name = "tagDAO")
    private TagDAO tagDAO;
    @Resource(name = "tagExpressDAO")
    private TagExpressDAO tagExpressDAO;
    @Resource(name = "userDAO")
    private UserDAO userDAO;
    @Resource(name = "userExpressService")
    private UserExpressService userExpressService;
    @Resource(name = "tempExpressDAO")
    private TempExpressDAO tempExpressDAO;
    @Resource()
    private DomainDAO domainDAO;

    @Override
    public Response cancelSign(LoginSession lg, String expressNo) {
        Express express = expressDAO.getExpressByNo(expressNo, lg.getRootDomainId());
        if (express == null) {
            return new Response(Response.ERROR);
        }
        return cancelSign(lg.getUserId(), express.getId());
    }

    @Override
    public Response cancelSign(int userId, int expressId) {
        Response res = new Response(Response.ERROR);

        Express express = expressDAO.getById(expressId);
        if (null == express) {
            res.setMessage("订单不存在");
            return res;
        }
        // 根据已经签收的订单编号获取订单用户关系
        UserExpress userExpress = userExpressDAO.getLastUserExpress(expressId);
//        if (null == userExpress || userExpress.getUserId() != userId) {
//            res.setMessage("只能由本人执行此操作");
//            return res;
//        }
        // 根据订单获取设备,因为已经误签收,所以查询设备时选择已经解绑的
        User user = userDAO.getById(userId);
        if (null == user) {
            res.setMessage("用户不存在");
            return res;
        }

        TagExpress tagExpress = tagExpressDAO.getLastTagExpressByEId(express.getId());
        if (null == tagExpress) {
            res.setMessage("未找到原来绑定的设备");
            return res;
        }

        Tag tag = tagDAO.getById(tagExpress.getTagNo());
        if (null != tag && null != tag.getDomainId() && tag.getDomainId().equals(user.getRootDomainId())) {
            //根据角色ID判断用户是否为医药版或标准版
            if (userDAO.getUserRole(userId).get(0).getId() >= Constants.Role.ADMIN_MEDICINE) {
                //医药版/标准版 需要判断设备是否已经在使用中
                List<TagExpress> binds = tagExpressDAO
                        .getTagExpressByTagNoAndStatus(tag.getTagNo(), Constants.TagExpressState
                                .STATE_ACTIVE);
                if (null != binds && binds.size() > 0) {
                    res.setMessage("该设备已在使用中！");
                    return res;
                }
            }
            // 撤销还需要,涉及tagExpress操作,只能根据时间获取最后一次的TagExpress,并将其状态变为1
            tagExpress.setStatus(Constants.State.STATE_ACTIVE);
            // 撤销订单签收相关信息,变回转运状态
//            express.setDomainId(user.getRootDomainId());
            express.setLastModitied(new Date());
            express.setStatus(Constants.ExpressState.STATE_ACTIVE);
            express.setCheckOutTime(null);
            String pattern = ".*\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\)";
            String expressNo = express.getExpressNo();
            if (!StringUtils.isEmpty(expressNo) && Pattern.matches(pattern, expressNo)) {
                express.setExpressNo(expressNo.substring(0, expressNo.length() - 21));
            }
            // 撤销用户订单签收关系,改回转运状态
            userExpress.setLastModitied(new Date());
            userExpress.setStatus(Constants.State.STATE_ACTIVE);

            res.setMessage("撤销签收成功");
            res.setCode(Response.OK);
        } else {
            res.setMessage("设备已删除");
        }
        return res;
    }

    @Override
    public Response saveTakingExpress(int userId, String expressNo, String tagNo, String description, Integer
            appointStart, Integer appointEnd, int flag) {
        Date now = new Date();
        Response res = new Response(Response.ERROR);
        User user = userDAO.getById(userId);
        Tag tag = tagDAO.getById(tagNo);

        if (null == user) {
            res.setMessage("用户错误！");
            return res;
        }
        if (tag == null) {
            res.setMessage("没有该设备");
            return res;
        }
        if (null != tag.getElectricity() && tag.getElectricity() != 1 && tag.getElectricity() < Constants.Electricity.NORMAL) {
            res.setMessage("电量不足");
            return res;
        }
        if (tag.getDomainId() == null || tag.getDomainId() != user.getRootDomainId()) {
            res.setMessage("失败，非本站点注册设备！");
            return res;
        }

        if (flag > 1) {
            List<TagExpress> tagExpressList = tagExpressDAO.getTagExpressByTagNoAndStatus(tagNo, Constants
                    .TagExpressState.STATE_ACTIVE);
            if (null != tagExpressList && tagExpressList.size() >= 1) {
                List<Express> expresses = new ArrayList<>();
                for (TagExpress tagExpress : tagExpressList) {
                    Express express = expressDAO.getById(tagExpress.getExpressId());
                    if (null != express && express.getStatus() != Constants.ExpressState.STATE_FINISHED) {
                        //判断该硬件之前绑定的监测点是否成功
                        if (null != tag.getLastConnected() && tag.getLastConnected().after(express.getCreationTime())) {
                            expresses.add(express);
                        } else {
                            //设备存在未成功的监测点，则删除这些
                            userExpressDAO.deleteAllByExpressId(express.getId());
                            expressDAO.delete(express.getId());
                            tagExpressDAO.delete(tagExpress.getId());
                        }
                    }
                }
                if (!expresses.isEmpty()) {
                    res.setMessage("该设备存在未结束的监测点！");
                    return res;
                }
            }
        }

        if (tag.getSleepTime() == 0) {
            tag.setSleepTime(15);
        }
        Express express = expressDAO.getExpressByNo(expressNo, user.getRootDomainId());
        if (express == null) {
            express = new Express();
            express.setDomainId(user.getRootDomainId());
            express.setExpressNo(expressNo);
            express.setLastModitied(now);
            express.setCreationTime(now);
            express.setDescription(description);
            if (null != tag.getExpressSleepTime() && tag.getExpressSleepTime() > 0) {
                express.setSleepTime(tag.getExpressSleepTime());
            } else {
                express.setSleepTime(tag.getSleepTime());
            }
            if (null != tag.getTemperatureMax()) {
                express.setTemperatureMax(tag.getTemperatureMax());
            }
            if (null != tag.getTemperatureMin()) {
                express.setTemperatureMin(tag.getTemperatureMin());
            }
            express.setAppointStart(appointStart);
            express.setAppointEnd(appointEnd);
            express.setStatus(Constants.ExpressState.STATE_ACTIVE);
            expressDAO.save(express);
            userExpressService.createUserExpressRelation(userId, express.getId());
        } else {
            if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
                res.setMessage("名称曾经被使用过！");
                return res;
            }
            //存在同名监测点或订单时
            TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(express.getId());
            if (null != tagExpress) {
                Tag tagTemp = tagDAO.getById(tagExpress.getTagNo());
                if (null != tagTemp) {
                    if (null != tagTemp.getLastConnected() && tagTemp.getLastConnected().after(express
                            .getCreationTime())) {
                        res.setMessage("名称已存在，请更改！");
                        return res;
                    }
                }
            }

        }

        express.setCreationTime(new Date());
        // 如果订单之前绑定设备了,则解绑之前的设备
        // 根据订单编号获取之前绑定的设备,然后解绑
        TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(express.getId());
        // 如果存在绑定关系则更新,设为delete
        if (tagExpress != null) {
            tagExpress.setStatus(Constants.BindState.STATE_DELETE);
            tagExpress.setLastModified(now);
            tagExpressDAO.update(tagExpress);
        }

        TagExpress te1 = new TagExpress();
        te1.setCreationTime(now);
        te1.setDomainId(user.getRootDomainId());
        te1.setExpressId(express.getId());
        te1.setLastModified(now);
        te1.setStatus(Constants.BindState.STATE_ACTIVE);
        te1.setTagNo(tagNo);
        tagExpressDAO.save(te1);

        // 设备启用,
        tag.setStatus(Constants.TagState.STATE_WORKING);
        tag.setExpressSleepTime(express.getSleepTime());
        tag.setAlertPhones(user.getMobile() + ";");
        tagDAO.save(tag);

        res.setCode(0);
        res.setMessage("成功");
        return res;
    }

    @Override
    public Response signExpressByIdList(Set<Integer> expressIdSet) {
        Response res = new Response(Response.ERROR);
        if (null == expressIdSet || expressIdSet.isEmpty()) {
            res.setMessage("输入为空！");
            return res;
        }
        try {
            for (Integer expressId : expressIdSet) {
                // 第一步,获取订单,然后更新,第一张表的操作
                if (null == expressId) {
                    continue;
                }
                Express express = expressDAO.getById(expressId);
                if (null == express) {
                    continue;
                }
                //对订单没有设置的参数，将设备参数同步过来
                TagExpress lastTagExpress = tagExpressDAO.getLastTagExpressByEId(expressId);
                if (null != lastTagExpress) {
                    Tag tag = tagDAO.getById(lastTagExpress.getTagNo());
                    if (null != tag) {
                        if (null == express.getSleepTime()) {
                            if (null != tag.getExpressSleepTime()) {
                                express.setSleepTime(tag.getExpressSleepTime());
                            } else if (null != tag.getSleepTime()) {
                                express.setSleepTime(tag.getSleepTime());
                            }
                        }
                        if (null == express.getTemperatureMax() && null != tag.getTemperatureMax()) {
                            express.setTemperatureMax(tag.getTemperatureMax());
                        }
                        if (null == express.getTemperatureMin() && null != tag.getTemperatureMin()) {
                            express.setTemperatureMin(tag.getTemperatureMin());
                        }
//                        if (null == express.getAppointStart() && null != tag.getAppointStart()) {
//                            express.setAppointStart(tag.getAppointStart());
//                        }
                    }
                }

                express.setLastModitied(new Date());
                express.setCheckOutTime(new Date());
                //对结束的加时间戳
                express.setExpressNo(express.getExpressNo() + "(" + Utils.SF.format(new Date()) + ")");
                express.setStatus(Constants.ExpressState.STATE_FINISHED);
                // 第二步,获取用户订单关系,第二张表的操作
                List<UserExpress> list = userExpressDAO.getUserExpressByEId(express.getId());
                if (null != list && !list.isEmpty()) {
                    for (UserExpress userExpress : list) {
                        userExpress.setLastModitied(new Date());
                        userExpress.setStatus(Constants.State.STATE_FINISHED);
                    }
                }
                // 设备订单关系放在handler中处理，因为要接收离线数据，直接解除关系会导致离线数据丢失。

            }
            res.setCode(0);
            res.setMessage("成功");
        } catch (Exception e) {
            res.setMessage("失败");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public Response saveExpressAttribute(String expressValue, String expressFlag, int userId, int expressId) {
        // java1.7之后支持String类型的switch case
        Response res = new Response(Response.ERROR);
        if (StringUtils.isEmpty(expressFlag) || StringUtils.isEmpty(expressValue)) {
            return res;
        }
        try {
            switch (expressFlag) {
                case "tempLimit":
                    String[] temperature = expressValue.split(",");
                    Float maxTemp = null;
                    Float minTemp = null;
                    if (temperature.length == 2) {
                        if (!StringUtils.isEmpty(temperature[0])) {
                            maxTemp = Float.parseFloat(temperature[0]);
                        }
                        if (!StringUtils.isEmpty(temperature[1])) {
                            minTemp = Float.parseFloat(temperature[1]);
                        }
                    } else if (temperature.length == 1) {
                        if (!StringUtils.isEmpty(temperature[0])) {
                            maxTemp = Float.parseFloat(temperature[0]);
                        }
                    } else {
                        return res;
                    }
                    editTemperature(expressId, maxTemp, minTemp);
                    break;
                case "period":
                    editSleepTime(expressId, Integer.parseInt(expressValue));
                    break;
                case "desc":
                    expressDAO.saveExpressDesc(expressId, expressValue);
                    break;
                case "beginTime":
                    Integer appointStart = Integer.parseInt(expressValue);
                    expressDAO.saveExpressAppointStart(expressId, appointStart);
                    break;
                case "endTime":
                    Integer appointEnd = Integer.parseInt(expressValue);
                    expressDAO.saveExpressAppointEnd(expressId, appointEnd);
                    break;
            }
            res.setCode(Response.OK);
            res.setMessage("操作成功");
        } catch (Exception e) {
            res.setCode(Response.ERROR);
            res.setMessage("操作失败");
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Response editSleepTime(int expressId, int time) {
        Response res = new Response();
        try {
            // 获得绑定的订单设备关系
            TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(expressId);
            // 根据设备id获得所有绑定的订单
            List<TagExpress> list = tagExpressDAO.getTagExpressByTagNoAndStatus(tagExpress.getTagNo(), Constants
                    .TagExpressState.STATE_ACTIVE);
            // 遍历,将所有订单的sleepTime变为设置的值
            for (TagExpress te : list) {
                Express express = expressDAO.getById(te.getExpressId());
                express.setSleepTime(time);
            }
            // 再将设备sleeptime变为这个值
            Tag tag = tagDAO.getById(tagExpress.getTagNo());
            tag.setExpressSleepTime(time);
            if (domainDAO.getById(tag.getDomainId()).getVersion() > Constants.Version.EXPRESS) {
                tag.setSleepTime(time);
            }

            res.setCode(0);
            res.setMessage("设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.setCode(1);
            res.setMessage("设置失败");
        }
        return res;
    }

    @Override
    public Response editAppointStart(Integer expressId, String time) {
        Response res = new Response();
        try {
            Express express = expressDAO.getById(expressId);
            express.setAppointStart(Integer.parseInt(time));
            res.setCode(0);
            res.setMessage("设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.setCode(1);
            res.setMessage("设置失败");
        }
        return res;
    }

    @Override
    public Response editAppointEnd(Integer expressId, String time) {

        Response res = new Response(Response.ERROR);
        if (null == expressId || null == time || time.isEmpty()) {
            return res;
        }
        try {
            Express express = expressDAO.getById(expressId);
            express.setAppointEnd(Integer.parseInt(time));
            res.setCode(0);
            res.setMessage("设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            res.setMessage("设置失败");
        }
        return res;
    }

    @Override
    public Response editTemperature(Integer expressId, Float min, Float max) {
        Response response = new Response(Response.ERROR);
        if (null == expressId) {
            return response;
        }
        if (null != min && null != max && min >= max) {
            return response;
        }
        Express express = getById(expressId);
        if (null != express) {
            express.setTemperatureMax(max);
            express.setTemperatureMin(min);
        }
        //订单设置温度上下限后，需将值更新至硬件设置中
        TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(expressId);
        if (null != tagExpress) {
            Tag tag = tagDAO.getById(tagExpress.getTagNo());
            if (null != tag) {
                tag.setExpressTMax(max);
                tag.setExpressTMin(min);
                if (domainDAO.getById(tag.getDomainId()).getVersion() > Constants.Version.EXPRESS) {
                    tag.setTemperatureMax(max);
                    tag.setTemperatureMin(min);
                }
            }
        }
        response.setCode(Response.OK);
        return response;
    }

    @Override
    public boolean syncTagParam(int expressId, Float min, Float max, Integer time) {
        expressDAO.saveExpressTemperature(expressId, max, min);
        if (null == time) {
            return false;
        }
        expressDAO.saveExpressSleepTime(expressId, time);
        return true;
    }

    @Override
    public List<Express> getExpressesByTagNo(String tagNo) {
        if (StringUtils.isEmpty(tagNo)) {
            return null;
        }

        List<TagExpress> tagExpressList = tagExpressDAO.getTagExpressByTagNoAndStatus(tagNo, Constants
                .TagExpressState.STATE_ACTIVE);
        if (null == tagExpressList || tagExpressList.size() < 1) {
            return null;
        }

        List<Express> expressList = new ArrayList<>();
        for (TagExpress tagExpress : tagExpressList) {
            Express express = getById(tagExpress.getExpressId());
            if (null != express) {
                expressList.add(express);
            }
        }
        return expressList;
    }

    @Override
    public List<Express> getExpressListByIds(List<Integer> idList) {
        if (null == idList || idList.size() < 1) {
            return null;
        }
        return expressDAO.getByIdList(idList);
    }


    //此方法较费时
    @Override
    public boolean isDataComplete(Express express) {
        TagExpress tagExpressByEId = tagExpressDAO.getTagExpressByEId(express.getId());
        if (null == tagExpressByEId) {
            return true;
        }
        Tag tag = tagDAO.getById(tagExpressByEId.getTagNo());
        Date now = new Date();
        int delay = 0;
        if (express.getAppointStart() != null) {
            delay = express.getAppointStart();
        } else if (tag.getAppointStart() != null) {
            delay = tag.getAppointStart();
        }
        Date appointStart = new Date();
        appointStart.setTime(express.getCreationTime().getTime() + delay * 60000L);
        if (appointStart.after(now)) {
            return true;
        }
        TempExpress newsetOneByExpressId = tempExpressDAO.getNewsetOneByExpressId(express.getId());
        if (null == newsetOneByExpressId) {
            return false;
        }
        Integer sleepTime = null;
        if (null != express.getSleepTime()) {
            sleepTime = express.getSleepTime();
        } else {
            if (null != tag.getExpressSleepTime()) {
                sleepTime = tag.getExpressSleepTime();
            } else if (null != tag.getSleepTime()) {
                sleepTime = tag.getSleepTime();
            }
        }
        if (null != sleepTime) {
            if (null != express.getCheckOutTime()) {
                now.setTime(express.getCheckOutTime().getTime());
            }
            now.setTime(now.getTime() - sleepTime * 120000L);
            return newsetOneByExpressId.getCreationTime().after(now);
        }
        return false;
    }

    @Override
    public String getTagNoByExpressId(int expressId) {
        TagExpress lastTagExpressByEId = tagExpressDAO.getLastTagExpressByEId(expressId);
        if (null == lastTagExpressByEId) {
            return null;
        } else {
            return lastTagExpressByEId.getTagNo();
        }
    }

    @Override
    public Express getExpressByExpressNoAndDomainId(String expressNo, Integer domainId) {
        if (StringUtils.isEmpty(expressNo) || null == domainId) {
            return null;
        }
        return expressDAO.getExpressByNo(expressNo, domainId);
    }

    //获取站点及子孙站点的所有人员订单
    @Override
    public List<Express> getExpressesByDomainId(int domainId, boolean isCurrent) {
        List<Domain> domainList = domainDAO.getSonDomainList(domainId);
        //将自身站点人员加入到人员列表
        List<User> userList = userDAO.getUsersByDomainId(domainId);
        if (null != domainList && domainList.size() > 0) {
            for (Domain domain : domainList) {
                List<User> users = userDAO.getUsersByDomainId(domain.getId());
                if (null != users && users.size() > 0) {
                    //将各子孙站点人员加入到列表
                    userList.addAll(users);
                }
            }
        }
        if (null == userList || userList.size() < 1) {
            return null;
        }
        Set<Integer> currentIds = new HashSet<>();
        Set<Integer> hisIds = new HashSet<>();
        for (User user : userList) {
            List<Integer> currentList = userExpressDAO.getExpressIdListByUserId(user.getId(), Constants
                    .UserExpressState.STATE_ACTIVE);
            if (null != currentList && currentList.size() > 0) {
                currentIds.addAll(currentList);
            }
            List<Integer> hisList = userExpressDAO.getExpressIdListByUserId(user.getId(), Constants
                    .UserExpressState.STATE_FINISHED);
            if (null != hisList && hisList.size() > 0) {
                hisIds.addAll(hisList);
            }
        }
        if (currentIds.size() > 0 && hisIds.size() > 0) {
            for (Integer i : hisIds) {
                if (currentIds.contains(i)) {
                    hisIds.remove(i);
                }
            }
        }

        if (isCurrent) {
            return getExpressListByIds(new ArrayList<>(currentIds));
        } else {
            return getExpressListByIds(new ArrayList<>(hisIds));
        }

    }

    @Override
    public List<Express> getExpressList(int userId, boolean isCurrent, boolean isConsiderDomainRights, Integer
            offset, Integer limit, String startTime, String endTime, String userName, boolean brief) {

        User user = userDAO.getById(userId);
        if (null == user) {
            return null;
        }

        Date startDate = null;
        Date endDate = null;
        if (!StringUtils.isEmpty(startTime)) {
            try {
                startDate = Utils.SF.parse(startTime);
            } catch (ParseException e) {
                e.printStackTrace();
                startDate = null;
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            try {
                endDate = Utils.SF.parse(endTime);
            } catch (ParseException e) {
                e.printStackTrace();
                endDate = null;
            }
        }

        List<Role> roles = userDAO.getUserRole(userId);
        List<Express> expresses;
        if (null != roles && !roles.isEmpty()) {
            Role role = roles.get(0);
            if (isConsiderDomainRights && role.isDomainRights()) {
                expresses = getExpressesByDomainId(user.getDomainId(), isCurrent);
            } else {
                if (isCurrent) {
                    //用户的当前订单
                    List<Integer> expressIdList = userExpressDAO.getExpressIdListByUserId(userId, Constants
                            .UserExpressState.STATE_ACTIVE);
                    expresses = new ArrayList<>();
                    if (!expressIdList.isEmpty()) {
                        for (Integer i : expressIdList) {
                            expresses.add(getById(i));
                        }
                    }
                } else {
                    //用户的历史订单
                    List<Integer> expressIdList = userExpressDAO.getExpressIdListByUserId(userId, Constants
                            .UserExpressState.STATE_FINISHED);
                    expresses = new ArrayList<>();
                    if (!expressIdList.isEmpty()) {
                        for (Integer i : expressIdList) {
                            expresses.add(getById(i));
                        }
                    }
                }
            }
            if (null != expresses && expresses.size() > 0) {

                List<Express> expressTemp = new ArrayList<>();

                if (isCurrent) {
                    for (Express express : expresses) {
                        if (null != express.getCreationTime()) {
                            if (null != startDate && express.getCreationTime().before(startDate)) {
                                continue;
                            }
                            if (null != endDate && express.getCreationTime().after(endDate)) {
                                continue;
                            }
                        }
                        expressTemp.add(express);
                    }
                } else {
                    for (Express express : expresses) {
                        if (null != express.getCheckOutTime()) {
                            if (null != startDate && express.getCheckOutTime().before(startDate)) {
                                continue;
                            }
                            if (null != endDate && express.getCheckOutTime().after(endDate)) {
                                continue;
                            }
                        }
                        expressTemp.add(express);
                    }
                }
                expresses = expressTemp;

                expressTemp = new ArrayList<>();
                for (Express express : expresses) {
                    UserExpress userExpress = userExpressDAO.getLastUserExpress(express.getId());
                    String nickName = userDAO.getById(userExpress.getUserId()).getNickName();
                    if (!StringUtils.isEmpty(nickName)) {
                        express.setUserName(nickName);
                    }
                    if (StringUtils.isEmpty(userName)) {
                        expressTemp.add(express);
                    } else if (nickName.contains(userName)) {
                        expressTemp.add(express);
                    }
                }
                expresses = expressTemp;

                if (!brief) {
                    //若是为了统计查询的，就不进行以下的步骤

                    //排序
                    Collections.sort(expresses, new Comparator<Express>() {
                        @Override
                        public int compare(Express o1, Express o2) {
                            if (null != o1.getCheckOutTime() && null != o2.getCheckOutTime()) {
                                if (o1.getCheckOutTime().after(o2.getCheckOutTime())) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            } else if (null == o1.getCheckOutTime() && null == o2.getCheckOutTime()) {
                                if (o1.getCreationTime().after(o2.getCreationTime())) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            } else if (null == o1.getCheckOutTime()) {
                                return -1;
                            } else if (null == o2.getCheckOutTime()) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });


                    if (null != offset && null != limit) {
                        if (offset * limit >= expresses.size()) {
                            return null;
                        } else {
                            int endIdx;
                            if ((offset + 1) * limit >= expresses.size()) {
                                endIdx = expresses.size();
                            } else {
                                endIdx = (offset + 1) * limit;
                            }
                            expresses = expresses.subList(offset * limit, endIdx);
                        }
                    }
                }

                for (Express express : expresses) {
                    if (!brief && !isCurrent) {
                        //历史订单判断其数据完整性
                        //此操作较费时
                        express.setDataComplete(isDataComplete(express));
                    }
                }
            }
            return expresses;
        }
        return null;
    }

    @Override
    public List<Integer> getExpressListByExpressNoList(int userId, List<String> expressNoList) {
        List<Integer> expressIdList = userExpressDAO.getExpressIdListByUserId(userId, Constants
                .UserExpressState.STATE_ACTIVE);
        if (null == expressNoList || expressNoList.size() < 1) {
            return null;
        }
        if (null == expressIdList || expressIdList.size() < 1) {
            return null;
        }
        List<Express> expressList = getExpressListByIds(expressIdList);
        Set<Integer> expressIds = new LinkedHashSet<>();
        for (String expressNo : expressNoList) {
            if (StringUtils.isEmpty(expressNo)) {
                continue;
            }
            for (Express express : expressList) {
                if (express.getExpressNo().equals(expressNo)) {
                    expressIds.add(express.getId());
                    break;
                }
            }
        }
        if (expressIds.size() > 0) {
            return new ArrayList<>(expressIds);
        } else {
            return null;
        }
    }

    @Override
    public List<Express> queryByExpressNo(int userId, String expressNo) {
        User user = userDAO.getById(userId);
        if (null == user) {
            return null;
        }
        if (StringUtils.isEmpty(expressNo) || expressNo.contains("'")) {
            return null;
        }
        return expressDAO.queryByExpressNo(expressNo, user.getRootDomainId());
    }

    @Override
    public Response getMonitorsStatus(int userId) {
        Response response = new Response(Response.ERROR);
        if (userId <= 0) {
            return response;
        }
        User user = userDAO.getById(userId);
        if (null == user) {
            return response;
        }

        List<Express> monitors = getExpressList(userId, true, false, null, null, null, null, null, true);

        if (null == monitors || monitors.isEmpty()) {
            return response;
        }
        response.setExpress(monitors);
        for (Express monitor : response.getExpress()) {
            TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(monitor.getId());
            if (null == tagExpress) {
                monitor.setCreatMonitorStatus(-1);
                continue;
            }
            Tag tag = tagDAO.getById(tagExpress.getTagNo());
            if (null == tag) {
                monitor.setCreatMonitorStatus(-1);
                continue;
            }
            monitor.setBindHardType(tag.getHardwareType());
            Date lastConnected = tag.getLastConnected();
            Date creationTime = monitor.getCreationTime();
            if (null == lastConnected || null == creationTime) {
                monitor.setCreatMonitorStatus(-1);
                continue;
            }
            if (lastConnected.before(creationTime)) {
                monitor.setCreatMonitorStatus(-1);
            } else {
                if (null != monitor.getAppointStart()) {
                    monitor.setCreatMonitorStatus(monitor.getAppointStart());
                } else if (null != tag.getAppointStart()) {
                    monitor.setCreatMonitorStatus(tag.getAppointStart());
                } else {
                    monitor.setCreatMonitorStatus(0);
                }
            }
        }
        response.setCode(Response.OK);
        return response;
    }

    @Override
    public Response deleteFailedMonitor(int expressId) {
        Response response = new Response(Response.ERROR);
        Express express = expressDAO.getById(expressId);
        if (null == express || express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
            return response;
        }
        TagExpress tagExpress = tagExpressDAO.getTagExpressByEId(expressId);
        if (null == tagExpress) {
            return response;
        }
        Tag tag = tagDAO.getById(tagExpress.getTagNo());
        if (null == tag) {
            return response;
        }
        if (null == tag.getLastConnected() || express.getCreationTime().after(tag.getLastConnected())) {
            //可以删除
            userExpressDAO.deleteAllByExpressId(express.getId());
            expressDAO.delete(express.getId());
            tagExpressDAO.delete(tagExpress.getId());
            response.setCode(Response.OK);
            response.setMessage("删除成功！");
            return response;
        } else {
            response.setMessage("监测已成功，不可删除！");
            return response;
        }
    }

}
