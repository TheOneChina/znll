package com.web.service;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T> {

    void save(T entity);

    void update(T entity);

    void delete(Serializable id);

    Integer count(String hql, Object... params);

    List<T> getAll();

    T getById(Serializable id);

    List<T> getByHQL(String hql, Object... params);

    List<T> getByHQLWithLimits(Integer first, Integer rows, String hql, Object... params);
}
