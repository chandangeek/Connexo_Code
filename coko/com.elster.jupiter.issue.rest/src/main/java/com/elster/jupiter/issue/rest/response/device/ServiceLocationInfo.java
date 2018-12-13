/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.ServiceLocation;

public class ServiceLocationInfo {
    private long id;
    private String info;

    public ServiceLocationInfo(ServiceLocation location) {
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
