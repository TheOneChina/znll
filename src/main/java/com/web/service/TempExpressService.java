package com.web.service;

import com.tnsoft.web.model.Response;
import com.tnsoft.web.model.Result;

import java.util.Date;
import java.util.Map;

public interface TempExpressService {

    Result queryByExpressId(Integer expressId);

    Response getByExpressIdWithDateLimit(int expressId, Date start, Date end);

    Map<String, String> getBriefInfoByExpressId(Integer expressId);

}
