package com.web.dao;

import com.tnsoft.hibernate.model.NDAAlert;

import java.util.List;

public interface AlertDAO extends BaseDAO<NDAAlert> {

    List<NDAAlert> getAlertByTagNo(String tagNo);

    List<NDAAlert> getAlertsByExpressId(int expressId, int... status);

    NDAAlert getNewestOneByDomainId(int domainId);

    NDAAlert getNewestOneByExpressId(int expressId);

    int countAlerts(int expressId, int... status);

    int countAlertsByTime(int expresId, Float hour);
}
