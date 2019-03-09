package com.web.service.impl;

import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.web.service.DomainService;
import org.springframework.stereotype.Service;

@Service("domainService")
public class DomainServiceImpl extends BaseServiceImpl<Domain> implements DomainService {

    @Override
    public String getPreferencesByDomainId(int domainId) {
        Domain domain = getById(domainId);
        if (null == domain) {
            return null;
        }
        return domain.getPreferences();
    }
}
