package com.tnsoft.hibernate;

import org.hibernate.*;
import org.hibernate.jdbc.Work;

import java.io.Closeable;
import java.io.Serializable;

public class DbSession implements Closeable {
    private Session session;
    private Transaction tx;

    public DbSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }

    public void clear() {
        this.session.clear();
    }

    public void close() {
        this.rollback();
        this.session.close();
    }

    public Criteria createCriteria(Class<?> persistentClass) {
        return this.session.createCriteria(persistentClass);
    }

    public Criteria createCriteria(Class<?> persistentClass, String alias) {
        return this.session.createCriteria(persistentClass, alias);
    }

    public Query createFilter(Object obj, String filter) {
        return this.session.createFilter(obj, filter);
    }

    public Query createQuery(String queryString) {
        return this.session.createQuery(queryString);
    }

    public SQLQuery createSQLQuery(String queryString) {
        return this.session.createSQLQuery(queryString);
    }

    public void delete(Object obj) {
        this.session.delete(obj);
    }

    public void evict(Object obj) {
        this.session.evict(obj);
    }

    public void flush() {
        this.session.flush();
    }

    public void refresh(Object obj) {
        this.session.refresh(obj);
    }

    public void update(Object obj) {
        this.session.update(obj);
    }

    public Object get(Class<?> clazz, Serializable id) {
        return this.session.get(clazz, id);
    }

    public Serializable save(Object obj) {
        return this.session.save(obj);
    }

    public void saveOrUpdate(Object obj) {
        this.session.saveOrUpdate(obj);
    }

    public void replicate(Object obj, ReplicationMode mode) {
        this.session.replicate(obj, mode);
    }

    public void doWork(Work wrk) {
        this.session.doWork(wrk);
    }

    public Transaction beginTransaction() {
        if (!this.isActive()) {
            this.tx = this.session.beginTransaction();
        }

        return this.tx;
    }

    public void commit() {
        this.commit(false);
    }

    public void commit(boolean restart) {
        if (this.tx != null && this.tx.isActive()) {
            this.tx.commit();
            if (restart) {
                this.tx = this.session.beginTransaction();
            }
        }

    }

    public void rollback() {
        if (this.tx != null && this.tx.isActive()) {
            try {
                this.tx.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isActive() {
        return this.tx != null && this.tx.isActive();
    }

}
