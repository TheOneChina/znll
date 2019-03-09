package com.tnsoft.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public final class BaseHibernateUtils {
    private static SessionFactory sessionFactory;

    public BaseHibernateUtils() {
    }

    public static synchronized void bootstrap(Properties prop) {
        Configuration config = (new Configuration()).configure();
        if (prop != null && !prop.isEmpty()) {
            config.addProperties(prop);
        }

        ServiceRegistryBuilder srb = new ServiceRegistryBuilder();
        ServiceRegistry serviceRegistry = srb.applySettings(config.getProperties()).buildServiceRegistry();
        sessionFactory = config.buildSessionFactory(serviceRegistry);
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            bootstrap(null);
        }

        return sessionFactory;
    }

    public static DbSession newSession() {
        return new DbSession(getSessionFactory().openSession());
    }

    public static void shutdown() {
        getSessionFactory().close();
    }

    public static Object getEntity(Class<?> clazz, Serializable id) {
        DbSession session = newSession();

        Object var3;
        try {
            var3 = session.get(clazz, id);
        } finally {
            session.close();
        }

        return var3;
    }

    public static void addEntity(Object obj) {
        DbSession session = newSession();
        session.beginTransaction();

        try {
            session.save(obj);
            session.commit();
        } finally {
            session.close();
        }

    }

    public static Date timeAdd(int field, int amount) {
        return timeAdd(System.currentTimeMillis(), field, amount);
    }

    public static Date timeAdd(long start, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        cal.add(field, amount);
        return cal.getTime();
    }

    public static void updateEntity(Object obj) {
        DbSession session = newSession();
        session.beginTransaction();

        try {
            session.update(obj);
            session.commit();
        } finally {
            session.close();
        }

    }

    public static long nextCsn(DbSession session) {
        return nextSequence(session, "csn");
    }

    public static long nextSequence(DbSession session, String seqName) {
        SQLQuery query = session.createSQLQuery("SELECT nextval('" + seqName + "')");
        return ((BigInteger) query.uniqueResult()).longValue();
    }

    public static long currentCsn(DbSession session) {
        SQLQuery query = session.createSQLQuery("SELECT last_value from csn");
        return ((BigInteger) query.uniqueResult()).longValue();
    }

    public static void setLimit(Criteria criteria, int offset, int limit) {
        if (offset > 0) {
            criteria.setFirstResult(offset);
        }

        if (limit > 0) {
            criteria.setMaxResults(limit);
        }

    }

    public static void setLimit(Query query, int offset, int limit) {
        if (offset > 0) {
            query.setFirstResult(offset);
        }

        if (limit > 0) {
            query.setMaxResults(limit);
        }

    }

    private static final class HibernateDataSource implements DataSource {
        private Connection connection;
        private PrintWriter writer;

        public HibernateDataSource(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return this.connection;
        }

        public Connection getConnection(String username, String password) {
            return this.connection;
        }

        public PrintWriter getLogWriter() {
            return this.writer;
        }

        public void setLogWriter(PrintWriter out) {
            this.writer = out;
        }

        public void setLoginTimeout(int seconds) {
        }

        public int getLoginTimeout() {
            return 0;
        }

        public <T> T unwrap(Class<T> iface) {
            return DataSource.class.equals(iface) ? (T) this : null;
        }

        public boolean isWrapperFor(Class<?> iface) {
            return DataSource.class.equals(iface);
        }

        public java.util.logging.Logger getParentLogger() {
            return null;
        }
    }
}
