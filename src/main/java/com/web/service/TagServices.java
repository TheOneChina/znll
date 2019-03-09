package com.web.service;

import com.expertise.common.logging.Logger;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.util.DBUtils;
import org.hibernate.SQLQuery;

import java.util.Date;
import java.util.List;

/**
 * 自动定时脚本，用于检测硬件是否未响应
 *
 * @author z
 */
public class TagServices {

    public Runnable getTagNotResponseService() {
        return new TagNotResponseService();
    }

    public Runnable getTagAvailableService() {
        return new TagAvailableService();
    }

    /**
     * 根据设置的次数，该次数周期后不上传数据即为失联
     */
    public static final class TagNotResponseService implements Runnable {

        @Override
        public void run() {
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
                                db.flush();
                            }
                        }
                    }
                }
                db.commit();
            } finally {
                db.close();
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
