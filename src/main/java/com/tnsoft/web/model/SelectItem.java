package com.tnsoft.web.model;

import java.io.Serializable;

public class SelectItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public SelectItem() {
    }

    public SelectItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
