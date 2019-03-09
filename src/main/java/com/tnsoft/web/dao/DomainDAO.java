package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.Domain;

import java.util.List;

public interface DomainDAO extends BaseDAO<Domain> {

    //获得所有子孙节点
    List<Domain> getSonDomainList(int domainId);

}
