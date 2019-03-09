package com.tnsoft.web.service.impl;

import com.tnsoft.web.dao.BaseDAO;
import com.tnsoft.web.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Transactional
public class BaseServiceImpl<T> implements BaseService<T> {

    @Autowired
    private BaseDAO<T> baseDAO;

    @Override
    public void save(T entity) {
        baseDAO.save(entity);
    }

    @Override
    public void update(T entity) {
        baseDAO.update(entity);
    }

    @Override
    public void delete(Serializable id) {
        baseDAO.delete(id);
    }

    @Override
    public T getById(Serializable id) {
        return baseDAO.getById(id);
    }

    @Override
    public List<T> getByHQL(String hql, Object... params) {
        return baseDAO.getByHQL(hql, params);
    }

    @Override
    public List<T> getByHQLWithLimits(Integer first, Integer rows, String hql, Object... params) {
        return baseDAO.getByHQLWithLimits(first, rows, hql, params);
    }

    @Override
    public List<T> getAll() {
        return baseDAO.getAll();
    }

    @Override
    public Integer count(String hql, Object... params) {
        return baseDAO.count(hql, params);
    }

}
