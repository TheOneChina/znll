package com.tnsoft.web.model;

import java.io.Serializable;
import java.util.List;

public class ExpressGatherInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private String tagNo;
    private List<ExpressNoAndDescription> expressNoAndDescriptions;


    public int getUserId() {
        return userId;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }


    public String getTagNo() {
        return tagNo;
    }


    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }


    public List<ExpressNoAndDescription> getExpressNoAndDescriptions() {
        return expressNoAndDescriptions;
    }


    public void setExpressNoAndDescriptions(List<ExpressNoAndDescription> expressNoAndDescriptions) {
        this.expressNoAndDescriptions = expressNoAndDescriptions;
    }


    public class ExpressNoAndDescription {
        private String expressNo;
        private String description;

        public String getExpressNo() {
            return expressNo;
        }

        public void setExpressNo(String expressNo) {
            this.expressNo = expressNo;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

}
