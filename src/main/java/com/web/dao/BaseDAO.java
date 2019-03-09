package com.web.dao;

import java.io.Serializable;
import java.util.List;

public interface BaseDAO<T> {

    void save(T entity);

    void update(T entity);

    void delete(Serializable id);

    Integer count(String hql, Object... params);

    T getById(Serializable id);

    T getOneByHQL(String hql, Object... params);

    List<T> getAll();

    List<T> getByHQL(String hql, Object... params);

    List<T> getByHQLWithLimits(Integer first, Integer rows, String hql, Object... params);

    void CUDByHql(String hql, Object... params);

    void clear();

}
