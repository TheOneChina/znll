package com.tnsoft.web.util;

import com.expertise.common.codec.Hex;
import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.OperateLog;
import com.tnsoft.hibernate.model.User;
import org.apache.commons.collections.iterators.IteratorChain;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Utils {


    public static String[] chars = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    public static final boolean DEBUG = true;
    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
    public static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SF1 = new SimpleDateFormat("yyyy-MM-dd$HH:mm:ss");
    public static final SimpleDateFormat SF_NO_SECOND = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static byte[] md5(String pwd) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            return md5.digest(StringUtils.toBytesQuietly(pwd));
        } catch (NoSuchAlgorithmException e) {
        }
        return new byte[]{};
    }

    public static String minifiedJS(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL should not be blank");
        }

        if (url.toLowerCase().endsWith(".min.js")) {
            return url;
        }

        if (url.toLowerCase().startsWith("/resources/")) {
            return url.replaceAll("\\.js$", ".min.js");
        }

        return url;
    }

    public static class IterableIterator<T> implements Iterable<T> {

        private Iterator<T> iterator;

        private IterableIterator(final Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<T> iterator() {
            return this.iterator;
        }

        public static <T> Iterable<T> from(final Iterator<T> iterator) {
            return new IterableIterator<T>(iterator);
        }

        public static <T> Iterable<T> from(final Iterable<T> iterable) {
            return new IterableIterator<T>(iterable.iterator());
        }

        public static <T> Iterable<T> from(final Iterator<T> iterator1, final Iterator<T> iterator2) {
            @SuppressWarnings("unchecked") Iterator<T> iteratorChain = new IteratorChain(iterator1, iterator2);
            return new IterableIterator<T>(iteratorChain);
        }

        public static <T> Iterable<T> from(final Iterable<T> iterable1, final Iterable<T> iterable2) {
            return IterableIterator.from(iterable1.iterator(), iterable2.iterator());
        }
    }

    public static void showParams(HttpServletRequest request) {
        Map map = new HashMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }

        Set<Map.Entry<String, String>> set = map.entrySet();
        for (Map.Entry entry : set) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public static String getUUID() {
        StringBuilder sb = new StringBuilder();
        //32位
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        sb.append(uuid);
        //补齐40
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[4];
        random.nextBytes(buf);
        sb.append(Hex.toHexString(buf));

        return sb.toString();
    }


    public static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();

    }

    public static void saveLog(int userId, String operation) {
        DbSession session = BaseHibernateUtils.newSession();
        try {
            session.beginTransaction();

            User user = (User) session.get(User.class, userId);

            OperateLog log = new OperateLog();
            log.setOperation(operation);
            log.setDomainId(user.getDomainId());
            log.setOperationTime(new Date());
            log.setUserId(userId);
            log.setUserName(user.getNickName());

            session.save(log);

            session.commit();
        } finally {
            session.close();
        }
    }

    public static byte[] newPassword(String pwd) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            return md5.digest(StringUtils.toBytesQuietly(pwd));
        } catch (NoSuchAlgorithmException e) {
        }
        return new byte[]{};
    }

    public static byte[] hash(String userName, byte[] passwd) throws GeneralSecurityException {
        MessageDigest md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
        md5.update(StringUtils.toBytesQuietly(userName));
        md5.update(passwd);
        return md5.digest();
    }

    public static boolean isMobileNO(String mobiles) {
        if (mobiles == null || mobiles.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile("^(1[3|4|5|7|8])\\d{9}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isValidAPName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile("[A-Za-z0-9_\\-]{4,}");
        Matcher m = p.matcher(name);
        return m.matches();
    }

    public static boolean isValidAPPasswd(String passwd) {
        if (passwd == null || passwd.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile("[A-Za-z0-9]{8,}");
        Matcher m = p.matcher(passwd);
        return m.matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile("[A-Za-z0-9]{8,}");
        Matcher m = p.matcher(username);
        return m.matches();
    }

    public static boolean isValidUserPasswd(String passwd) {
        if (passwd == null || passwd.isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile("[A-Za-z0-9]{6,}");
        Matcher m = p.matcher(passwd);
        return m.matches();
    }

    //利用java反射机制将任意对象的转换为map存储 .............2017.6.19 冯彬彬
    public static Map<String, Object> ObjToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        // System.out.println(obj.getClass());  
        // 获取f对象对应类中的所有属性域  
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0, len = fields.length; i < len; i++) {
            String varName = fields[i].getName();
            try {
                // 获取原来的访问控制权限  
                boolean accessFlag = fields[i].isAccessible();
                // 修改访问控制权限  
                fields[i].setAccessible(true);
                // 获取在对象f中属性fields[i]对应的对象中的变量  
                Object o = fields[i].get(obj);
                if (o != null)
                    map.put(varName, o);
                // System.out.println("传入的对象中包含一个如下的变量：" + varName + " = " + o);  
                // 恢复访问控制权限  
                fields[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return map;

    }


}
