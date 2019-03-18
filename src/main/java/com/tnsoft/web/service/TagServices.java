package com.tnsoft.web.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.SmsUtil;
import com.tnsoft.web.util.Utils;
import org.hibernate.SQLQuery;
import java.util.Date;
import java.util.List;

/**
 * 自动定时脚本，用于检测硬件是否未响应
 *
 * @author z
 */
public class TagServices {

    /**
     * 短信报警间隔
     */
    private static final long SMS_ALERT_CYCLE = 3600000L;

    public Runnable getTagNotResponseService() {
        return new TagNotResponseService();
    }

    public Runnable getTagAvailableService() {
        return new TagAvailableService();
    }

    /**
     * 根据设置的次数，该次数周期后不上传数据即为失联
     */

    public static final class TagNotResponseService implements Runnable{

        @Override
        public  void run() {
            DbSession db = BaseHibernateUtils.newSession();
            try {
                db.beginTransaction();
                List<AlertLevel> levels = DBUtils.getAllActiveNoResponseAlertLevel(db);

                for (AlertLevel level : levels) {
                    if (level != null) {
                        int times = level.getTimes();
                        Date now = new Date();
                        long nowTime = now.getTime();
                        List<Tag> onlineTags = DBUtils.getWorkingOnlineTags(db, level.getDomainId());
                        if (null == onlineTags) {
                            continue;
                        }
                        for (Tag tag : onlineTags) {
                            int sleepTime = 0;
                            if (null != tag.getExpressSleepTime() && tag.getExpressSleepTime() > 0) {
                                sleepTime = tag.getExpressSleepTime();
                            } else if (null != tag.getSleepTime() && tag.getSleepTime() > 0) {
                                sleepTime = tag.getSleepTime();
                            }
                            if (sleepTime == 0) {
                                continue;
                            }
                            long seconds = sleepTime * 60 * 1000 * times;
                            if (null != tag.getLastConnected() && tag.getLastConnected().getTime() + seconds >
                                    nowTime) {
                                //不需要报警；
                                continue;
                            }
                            List<Express> expressList = getExpressByTag(db, tag.getTagNo());
                            if (null == expressList || expressList.size() < 1) {
                                continue;
                            }
                            for (Express express : expressList) {
                                NDAAlert alert = new NDAAlert();
                                alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
                                alert.setCreationTime(now);
                                UserExpress userExpress = DBUtils.getUserByExpressId(db, express.getId());
                                if (null != userExpress) {
                                    alert.setDomainId(userExpress.getDomainId());
                                } else {
                                    alert.setDomainId(express.getDomainId());
                                }
                                alert.setExpressId(express.getId());
                                alert.setLastModitied(now);
                                alert.setStatus(Constants.AlertState.STATE_ACTIVE);
                                alert.setTagNo(tag.getTagNo());
                                alert.setType(Constants.AlertType.STATE_NOT_RESPONSE);
                                alert.setAlertTime(now);
                                db.save(alert);
                                express.addAlertCount();
                                db.update(express);
                                //设备失联警报持久化后，添加短信警报
                                sendLossSms(tag,db);
                                db.flush();
                            }
                        }
                    }
                }
                db.commit();
            }finally {
                db.close();
            }
        }

        public  void sendLossSms(Tag tag,DbSession db){
            try {
                Date lastSMSAlert = tag.getLastSMSAlert();
                Date dateTemp = new Date();
                String phones = tag.getAlertPhones();
                if (!phones.isEmpty()) {
                    String[] phonesArray = phones.split(";");
                    dateTemp.setTime(dateTemp.getTime() - SMS_ALERT_CYCLE);
                    if (null == lastSMSAlert || lastSMSAlert.before(dateTemp)) {
                        boolean flag = false;
                        String context = tag.getTagNo();
                        if (!StringUtils.isEmpty(tag.getName())) {
                            context = tag.getName();
                        }
                        for (String mobile : phonesArray) {
                            if (Utils.isMobileNO(mobile) && tag.getSms() > 0) {
                                if (!StringUtils.isEmpty(context) && !StringUtils.isEmpty(mobile) && Utils.isMobileNO(mobile)) {
                                    SendSmsResponse sendSmsResponse = null;
                                    try {
                                        sendSmsResponse = SmsUtil.sendAlertSms(mobile, context, Constants.SMSAlertType.TYPE_TEMP_LOSS);
                                    } catch (ClientException e) {
                                        e.printStackTrace();
                                    }
                                    if (null != sendSmsResponse) {
                                        SMSLog smsLog = new SMSLog(context, mobile, Constants.SMSAlertType.TYPE_TEMP_LOSS, sendSmsResponse.getMessage(), new Date(), tag.getDomainId());
                                        db.save(smsLog);
                                        tag.setSms(tag.getSms() - 1);
                                        flag = true;
                                    }
                                }
                            }
                        }
                        if (flag) {
                            tag.setLastSMSAlert(new Date());
                            //更新tag信息
                            db.update(tag);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

    }

    /**
     * 每天更新设备的可用性
     */
    public static final class TagAvailableService implements Runnable {

        @Override
        public void run() {
            Logger.error("##START TagAvailableService##");
            DbSession db = BaseHibernateUtils.newSession();
            try {
                db.beginTransaction();
                List<Tag> allAvailableTags = DBUtils.getAllAvailableTags(db);
                if (null != allAvailableTags && allAvailableTags.size() > 0) {
                    long now = System.currentTimeMillis();
                    for (Tag tag : allAvailableTags) {
                        if (null != tag.getServiceExpirationTime()) {
                            if (tag.getServiceExpirationTime().getTime() < now) {
                                tag.setStatus(Constants.TagState.STATE_DELETE);
                                db.update(tag);
                                db.flush();
                            }
                        }
                    }
                }
                db.commit();
            } finally {
                db.close();
            }
            Logger.error("##END TagAvailableService##");
        }
    }

    private static List<Express> getExpressByTag(DbSession db, String tagNo) {

        String sql = "SELECT * FROM nda_express WHERE id IN (SELECT express_id FROM nda_tag_express WHERE tag_no='"
                + tagNo + "' AND status=" + Constants.TagExpressState.STATE_ACTIVE + ") ";
        SQLQuery query = db.createSQLQuery(sql);
        query.addEntity(Express.class);
        return (List<Express>) query.list();

    }
}
