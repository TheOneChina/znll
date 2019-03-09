package com.tnsoft.web.service;

public interface SendAlertSMSService {
    boolean sendAlertSMS(String tagNo, String mobile, int type, int domainId);
}
