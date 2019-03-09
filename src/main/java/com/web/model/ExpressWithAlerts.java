package com.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpressWithAlerts implements Serializable {

    private static final long serialVersionUID = 1L;

    private int expressId;
    private int domainId;
    private String expressNo;
    private String mobile;
    private String userName;
    private int alertsCount = 0;
    private String alertsShow = "";

    private List<AlertElem> alertList = new ArrayList<AlertElem>();

    public int getExpressId() {
        return expressId;
    }

    public void setExpressId(int expressId) {
        this.expressId = expressId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<AlertElem> getAlertList() {
        return alertList;
    }

    public void addAlertElem(AlertElem alertElem) {
        alertList.add(alertElem);
        alertsCount++;
    }

    public int getAlertsCount() {
        return alertsCount;
    }

    public void setAlertsCount(int alertsCount) {
        this.alertsCount = alertsCount;
    }

    public void updateAlertsShow(String add) {
        alertsShow += add;
    }

    public String getAlertsShow() {
        return alertsShow;
    }

}