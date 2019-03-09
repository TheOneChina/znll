package com.web.dao.impl;

import com.tnsoft.hibernate.model.AlertLevel;
import com.tnsoft.web.dao.AlertLevelDAO;
import org.springframework.stereotype.Repository;

@Repository("alertLevelDAO")
public class AlertLevelDAOImpl extends BaseDAOImpl<AlertLevel> implements AlertLevelDAO {

    @Override
    public AlertLevel getAlertLevel(int domainId, int type) {
        String hql = "from AlertLevel where domainId=? and type=?";
        return getOneByHQL(hql, domainId, type);
    }
}
