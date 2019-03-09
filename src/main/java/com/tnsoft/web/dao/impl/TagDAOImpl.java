package com.tnsoft.web.dao.impl;

import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.web.dao.TagDAO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("tagDAO")
public class TagDAOImpl extends BaseDAOImpl<Tag> implements TagDAO {

    @Override
    public List<Tag> getTagsByDomainId(Integer domainId) {
        List<Tag> tags = new ArrayList<>();
        if (null != domainId && domainId > 0) {
            String hql = "from Tag where domainId=?";
            tags = getByHQL(hql, domainId);
        }
        return tags;
    }

}
