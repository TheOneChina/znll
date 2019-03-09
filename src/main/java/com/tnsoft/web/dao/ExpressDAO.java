package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.Express;

import java.util.List;

public interface ExpressDAO extends BaseDAO<Express> {

    Express getExpressByNo(String expressNo, int domainId);

    List<Express> getByIdList(List<Integer> idList);

    //模糊查询
    List<Express> queryByExpressNo(String expressNo, int domainId);

    List<Express> getAdminExpressToSign(Integer start, Integer rows, Integer domainId);

    Integer getCountByDomainId(Integer domainId);

    void saveExpressTemperature(int expressId, Float maxTemp, Float minTemp);

    void saveExpressSleepTime(Integer expressId, int sleepTime);

    void saveExpressDesc(Integer expressId, String desc);

    void saveExpressAppointStart(Integer expressId, Integer appointStart);

    void saveExpressAppointEnd(Integer expressId, Integer appointEnd);

    List<Express> getCurrentExpressesByDomainId(int domainId);

    List<Express> getHistoryExpressesByDomainId(int domainId);
}
