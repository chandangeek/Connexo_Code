package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.metering.ServiceLocation;

public class ServiceLocationPreviewInfo {
    private long id;
    private String info;

    public ServiceLocationPreviewInfo(ServiceLocation location) {
        this.id = location.getId();
        this.info = location.getAliasName();
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
