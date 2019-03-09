package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.TempExpress;

import java.util.Date;
import java.util.List;

public interface TempExpressDAO extends BaseDAO<TempExpress> {

    List<TempExpress> getByExpressId(int expressId);

    TempExpress getNewsetOneByExpressId(int expressId);

    List<TempExpress> getByExpressIdWithTimeLimit(int expressId, Date start, Date end);

    boolean hasData(int expressId);

}
