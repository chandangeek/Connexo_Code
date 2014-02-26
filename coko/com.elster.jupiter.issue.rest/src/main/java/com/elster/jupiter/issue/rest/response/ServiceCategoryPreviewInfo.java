package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.metering.ServiceCategory;

public class ServiceCategoryPreviewInfo {
    private long id;
    private String info;

    public ServiceCategoryPreviewInfo(ServiceCategory category) {
        this.id = category.getId();
        this.info = category.getAliasName();
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
