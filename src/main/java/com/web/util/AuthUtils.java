package com.web.util;

import com.expertise.common.codec.Hex;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.model.Constants;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class AuthUtils {

    public static final int AUTH_OK = 0;
    public static final int AUTH_DISABLED = -1;
    public static final int AUTH_ATTEMPT_EXCEED = -2;
    public static final int AUTH_FAILED = -3;

    private static final int MAX_ATTEMPT = 10;

    private AuthUtils() {
    }

    public static int authWithPassword(User user, String password, boolean reset) throws GeneralSecurityException {
        if (user.getStatus() != Constants.State.STATE_ACTIVE) {
            return AUTH_DISABLED;
        }

        int attempt = user.getAttempt();
        if (attempt > MAX_ATTEMPT) {
            return AUTH_ATTEMPT_EXCEED;
        }

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] buf = md5.digest(user.getPassword().getBytes());
        String rightPassword = Hex.toHexString(buf);

        if (!password.equals(rightPassword)) {
            user.setAttempt(user.getAttempt() + 1);
            return AUTH_FAILED;
        }
        if (user.getStatus() == Constants.UserState.STATE_CANCLE) {
            user.setAttempt(user.getAttempt() + 1);
            return AUTH_FAILED;
        }

        user.setAttempt(0);
        String ticket = user.getTicket();
        if (ticket == null || reset) {
            user.setTicket(newTicket());
        }
        return AUTH_OK;
    }

    public static User authWithTicket(DbSession session, String userName, String ticket) {
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("name", userName));
        User user = (User) criteria.uniqueResult();
        if (user == null || user.getStatus() != Constants.State.STATE_ACTIVE) {
            return null;
        }

        int attempt = user.getAttempt();
        if (attempt > MAX_ATTEMPT) {
            return null;
        }

        if (!ticket.equals(user.getTicket())) {
            user.setAttempt(user.getAttempt() + 1);
            return null;
        }

        user.setAttempt(0);
        return user;
    }

    private static String newTicket() {
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[16];
        random.nextBytes(buf);
        return Hex.toHexString(buf);
    }

}
