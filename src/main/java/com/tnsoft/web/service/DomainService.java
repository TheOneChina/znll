package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.Domain;

public interface DomainService extends BaseService<Domain> {

//	public List<Domain> getAllDomain();

    String getPreferencesByDomainId(int domainId);


}
