package com.web.dao.impl;

import com.tnsoft.web.dao.BaseDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

@Transactional
public class BaseDAOImpl<T> implements BaseDAO<T> {

    private Class<T> clazz;

    @SuppressWarnings("unchecked")
    public BaseDAOImpl() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        clazz = (Class<T>) type.getActualTypeArguments()[0];
    }

    @Resource
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return this.sessionFactory.getCurrentSession();
    }

    @Override
    public void save(T entity) {
        this.getSession().save(entity);
    }

    @Override
    public void update(T entity) {
        this.getSession().update(entity);
    }

    @Override
    public void delete(Serializable id) {
        this.getSession().delete(this.getById(id));
    }

    @Override
    public Integer count(String hql, Object... params) {
        Query query = this.getSession().createQuery(hql);
        for (int i = 0; null != params && i < params.length; i++) {
            query.setParameter(i, params[i]);
        }
        return query.list().size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getById(Serializable id) {
        return (T) this.getSession().get(this.clazz, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getOneByHQL(String hql, Object... params) {
        Query query = this.getSession().createQuery(hql);
        for (int i = 0; null != params && i < params.length; i++) {
            query.setParameter(i, params[i]);
        }
        return (T) query.uniqueResult();
    }

    @Override
    public List<T> getAll() {
        Criteria criteria = this.getSession().createCriteria(this.clazz);
        @SuppressWarnings("unchecked")
        List<T> list = criteria.list();
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getByHQL(String hql, Object... params) {
        Query query = this.getSession().createQuery(hql);
        for (int i = 0; null != params && i < params.length; i++) {
            query.setParameter(i, params[i]);
        }
        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getByHQLWithLimits(Integer first, Integer rows, String hql, Object... params) {
        Query query = this.getSession().createQuery(hql);
        for (int i = 0; null != params && i < params.length; i++) {
            query.setParameter(i, params[i]);
        }
        query.setFirstResult(first).setMaxResults(rows);
        return query.list();
    }

    @Override
    public void CUDByHql(String hql, Object... params) {
        Query query = this.getSession().createQuery(hql);
        for (int i = 0; null != params && i < params.length; i++) {
            query.setParameter(i, params[i]);
        }
        query.executeUpdate();
    }

    @Override
    public void clear() {
        this.getSession().clear();
    }

}
