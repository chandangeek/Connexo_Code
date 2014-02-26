package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.metering.UsagePoint;

public class UsagePointPreviewInfo {
    private long id;
    private String info;

    public UsagePointPreviewInfo(UsagePoint up) {
        this.id = up.getId();
        this.info = up.getAliasName();
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
