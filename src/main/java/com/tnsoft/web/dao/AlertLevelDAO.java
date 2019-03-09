package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.AlertLevel;

public interface AlertLevelDAO extends BaseDAO<AlertLevel> {

    AlertLevel getAlertLevel(int domainId, int type);

}
