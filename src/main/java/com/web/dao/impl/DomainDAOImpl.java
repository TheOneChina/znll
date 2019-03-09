package com.web.dao.impl;

import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.web.dao.DomainDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("domainDAO")
public class DomainDAOImpl extends BaseDAOImpl<Domain> implements DomainDAO {

    @Override
    public List<Domain> getSonDomainList(int domainId) {
        Domain domain = getById(domainId);
        if (null == domain) {
            return null;
        }
        String domainPath = domain.getDomainPath() + domainId + "/";
        String hql = "from Domain where domainPath like '%" + domainPath + "%'";
        return getByHQL(hql);
    }
}
