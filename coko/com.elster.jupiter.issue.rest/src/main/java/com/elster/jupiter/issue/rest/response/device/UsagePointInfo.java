/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.UsagePoint;

public class UsagePointInfo {
    private long id;
    private String info;

    public UsagePointInfo(UsagePoint up) {
        this.id = up.getId();
        this.info = up.getName();
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
