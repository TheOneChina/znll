package com.tnsoft.web.util;

import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.model.Constants;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用的数据库操作
 */
public final class DBUtils {

    public DBUtils() {
        super();
    }

    /**
     * 判断当前智能硬件是否绑定订单
     *
     * @param session
     * @param tagNo   智能硬件编码
     * @return
     */
    public static boolean hasBind(DbSession session, String tagNo) {
        Criteria criteria = session.createCriteria(TagExpress.class);
        criteria.add(Restrictions.eq("tagNo", tagNo));
        criteria.add(Restrictions.eq("status", Constants.BindState.STATE_ACTIVE));
        return !criteria.list().isEmpty();
    }

    /**
     * 判断手机号是否存在
     *
     * @param session
     * @param phone   手机号码
     * @return
     */
    public static boolean mobileExist(DbSession session, String phone) {
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("name", phone));
        return !criteria.list().isEmpty();
    }


    /**
     * 根据订单id获取订单所有经手过的配送员fbb
     *
     * @param db
     * @param expressId
     * @return
     */
    public static List<UserExpress> getUserExpressList(DbSession db, int expressId) {
        Criteria criteria = db.createCriteria(UserExpress.class);
        criteria.add(Restrictions.eq("expressId", expressId));
        criteria.addOrder(Order.asc("creationTime"));
        return criteria.list();
    }

    public static User getUserById(DbSession db, int userId) {
        Criteria criteria = db.createCriteria(User.class);
        criteria.add(Restrictions.eq("id", userId));
        return (User) criteria.uniqueResult();
    }

    /**
     * 根据用户id获取站点
     *
     * @param db
     * @param userId 用户id
     * @return
     */
    public static int getDomainIdByUserId(DbSession db, int userId) {
        Criteria criteria = db.createCriteria(User.class);
        criteria.add(Restrictions.eq("id", userId));
        User user = (User) criteria.uniqueResult();
        return user.getDomainId();
    }

    public static int getRootDomainIdByUserId(DbSession db, int userId) {
        Criteria criteria = db.createCriteria(User.class);
        criteria.add(Restrictions.eq("id", userId));
        User user = (User) criteria.uniqueResult();
        return user.getRootDomainId();
    }

    public static Domain getDomainByUserId(DbSession db, int userId) {
        Criteria criteria = db.createCriteria(Domain.class);
        criteria.add(Restrictions.eq("id", getDomainIdByUserId(db, userId)));
        return (Domain) criteria.uniqueResult();
    }

    /**
     * 根据订单id获取和用户的绑定关系
     *
     * @param db
     * @param expressId 订单id
     * @return
     */
    public static UserExpress getUserByExpressId(DbSession db, int expressId) {
        Criteria criteria = db.createCriteria(UserExpress.class);
        criteria.add(Restrictions.eq("expressId", expressId));
        criteria.add(Restrictions.eq("status", Constants.State.STATE_ACTIVE));
        return (UserExpress) criteria.uniqueResult();
    }

    public static UserRole getRoleByUserId(DbSession db, int userId) {
        Criteria criteria = db.createCriteria(UserRole.class);
        criteria.add(Restrictions.eq("userId", userId));
        return (UserRole) criteria.uniqueResult();
    }

    public static List<Role> getRoleListByUserId(DbSession db, int userId) {
        String sql = "SELECT * FROM nda_role r WHERE r.id IN ( SELECT role_id FROM user_role ur WHERE ur.user_id=" +
                userId + " ) order by r.id ASC";
        SQLQuery query = db.createSQLQuery(sql);
        query.addEntity(Role.class);
        return query.list();
    }

    public static Map<String, Object> getEmpty() {
        Map<String, Object> result = new HashMap<>();
        result.put("recordsTotal", 0);
        result.put("recordsFiltered", 0);
        result.put("data", Collections.emptyList());
        return result;
    }

    public static List<TempExpress> getAllTempesByExpressId(DbSession dbSession, int expressId) {
        Criteria criteria = dbSession.createCriteria(TempExpress.class);
        criteria.add(Restrictions.eq("expressId", expressId));
        criteria.addOrder(Order.asc("creationTime"));
        return criteria.list();
    }

    public static List<TempExpress> getAllTempesByExpressIdDesc(DbSession dbSession, int expressId) {
        Criteria criteria = dbSession.createCriteria(TempExpress.class);
        criteria.add(Restrictions.eq("expressId", expressId));
        criteria.addOrder(Order.desc("creationTime"));
        return criteria.list();
    }

    public static List<AlertLevel> getAllActiveNoResponseAlertLevel(DbSession db) {
        String sql = "SELECT * FROM nda_alert_level WHERE type=" + Constants.AlertLevelType.NO_RESPONSE + " AND status="
                + Constants.IsAble.ABLE;
        SQLQuery query = db.createSQLQuery(sql);
        query.addEntity(AlertLevel.class);
        return query.list();
    }

    public static List<Tag> getWorkingOnlineTags(DbSession db, int domainId) {
        String sql = "SELECT * FROM nda_tag WHERE domain_id=" + domainId + " AND status=" + Constants.TagState
                .STATE_WORKING;
        SQLQuery query = db.createSQLQuery(sql);
        query.addEntity(Tag.class);
        return query.list();
    }

    public static List<Tag> getAllAvailableTags(DbSession db) {
        String sql = "SELECT * FROM nda_tag WHERE status>" + Constants.TagState.STATE_DELETE;
        SQLQuery query = db.createSQLQuery(sql);
        query.addEntity(Tag.class);
        return query.list();
    }
}
