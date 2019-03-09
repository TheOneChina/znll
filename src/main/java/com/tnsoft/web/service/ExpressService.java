package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.Express;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;

import java.util.List;
import java.util.Set;

public interface ExpressService extends BaseService<Express> {

    Response cancelSign(LoginSession lg, String expressNo);

    Response cancelSign(int userId, int expressId);

    Response saveTakingExpress(int userId, String expressNo, String tagNo, String description, Integer appointStart,
                               Integer appointEnd, int flag);

    Response signExpressByIdList(Set<Integer> expressIdList);

    Response saveExpressAttribute(String expressValue, String expressFlag, int userId, int expressId);

    Response editSleepTime(int expressId, int time);

    Response editAppointStart(Integer expressId, String time);

    Response editAppointEnd(Integer expressId, String time);

    Response editTemperature(Integer expressId, Float min, Float max);

    //医药版设置设备时将设置同步至订单
    boolean syncTagParam(int expressId, Float min, Float max, Integer time);

    //获取设备当前绑定的所有订单
    List<Express> getExpressesByTagNo(String tagNo);

    List<Express> getExpressListByIds(List<Integer> idList);

    Express getExpressByExpressNoAndDomainId(String expressNo, Integer domainId);

    //根据用户id来查询当前订单，用户具有站点权限时返回站点的当前订单，否则返回与用户相关的当前订单。
    List<Express> getExpressList(int userId, boolean isCurrent, boolean isConsiderDomainRights, Integer offset,
                                 Integer limit, String startTime, String endTime, String userName, boolean brief);

    //获得订单id列表
    List<Integer> getExpressListByExpressNoList(int userId, List<String> expressNoList);

    //查询监测点是否启用成功
    Response getMonitorsStatus(int userId);

    //删除未成功的监测点
    Response deleteFailedMonitor(int expressId);

    //订单数据是否完整
    boolean isDataComplete(Express express);

    String getTagNoByExpressId(int expressId);

    //获取站点及子孙站点的所有人员订单
    List<Express> getExpressesByDomainId(int domainId, boolean isCurrent);

    List<Express> queryByExpressNo(int userId, String expressNo);
}
