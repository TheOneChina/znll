package com.web.dao;

import com.tnsoft.hibernate.model.Tag;

import java.util.List;

public interface TagDAO extends BaseDAO<Tag> {

    List<Tag> getTagsByDomainId(Integer domainId);

}
