package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.TagExpress;

import java.util.List;

public interface TagExpressDAO extends BaseDAO<TagExpress> {

    //不筛选状态
    List<TagExpress> getTagExpresses(String tagNo);

    //状态可用的
    TagExpress getTagExpressByEId(int expressId);

    //不筛选状态
    TagExpress getLastTagExpressByEId(int expressId);

    List<TagExpress> getTagExpressByTagNoAndStatus(String tagNo, int tagExpressStatus);
}
