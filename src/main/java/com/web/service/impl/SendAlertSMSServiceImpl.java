package com.web.service.impl;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.SMSLog;
import com.tnsoft.web.dao.SMSLogDAO;
import com.tnsoft.web.service.SendAlertSMSService;
import com.tnsoft.web.util.SmsUtil;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service("sendAlertSMSService")
public class SendAlertSMSServiceImpl implements SendAlertSMSService {

    @Resource(name = "smsLogDAO")
    private SMSLogDAO smsLogDAO;

    @Override
    public boolean sendAlertSMS(String tagNo, String mobile, int type, int domainId) {
        if (StringUtils.isEmpty(tagNo) || StringUtils.isEmpty(mobile) || !Utils.isMobileNO(mobile)) {
            return false;
        }
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = SmsUtil.sendAlertSms(mobile, tagNo, type);
        } catch (ClientException e) {
            e.printStackTrace();
        }
        if (null != sendSmsResponse) {
            SMSLog smsLog = new SMSLog(tagNo, mobile, type, sendSmsResponse.getMessage(), new Date(), domainId);
            smsLogDAO.save(smsLog);
            return sendSmsResponse.getCode().equals("OK");
        }

        return false;
    }
}
