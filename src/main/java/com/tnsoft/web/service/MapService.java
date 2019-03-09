package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.LocateExpress;

import java.util.List;

public interface MapService extends BaseService<LocateExpress> {

    List<LocateExpress> getLocationByExpressId(Integer expressId);

    void saveLocation(int userId, double lat, double lng, List<Integer> expressIds);
}
