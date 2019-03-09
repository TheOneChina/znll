package com.tnsoft.web.dao.impl;

import com.tnsoft.hibernate.model.TagExpress;
import com.tnsoft.web.dao.TagExpressDAO;
import com.tnsoft.web.model.Constants;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("tagExpressDAO")
public class TagExpressDAOImpl extends BaseDAOImpl<TagExpress> implements TagExpressDAO {
    @Override
    public List<TagExpress> getTagExpresses(String tagNo) {
        String hql = "from TagExpress where tagNo=? order by lastModified DESC";
        return getByHQL(hql, tagNo);
    }

    @Override
    public TagExpress getTagExpressByEId(int expressId) {
        String hql = "from TagExpress where expressId=? and status=?";
        return getOneByHQL(hql, expressId, Constants.State.STATE_ACTIVE);
    }

    @Override
    public TagExpress getLastTagExpressByEId(int expressId) {
        String hql = "from TagExpress where expressId=? order by lastModified DESC";
        List<TagExpress> list = getByHQL(hql, expressId);
        //根据时间排序之后,取得第一个,即为最后更新的
        return list.get(0);
    }

    @Override
    public List<TagExpress> getTagExpressByTagNoAndStatus(String tagNo, int tagExpressStatus) {
        String hql = "from TagExpress where tagNo=? and status=?";
        return getByHQL(hql, tagNo, tagExpressStatus);
    }

}
