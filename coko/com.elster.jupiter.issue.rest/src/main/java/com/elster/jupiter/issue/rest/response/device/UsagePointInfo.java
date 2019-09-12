/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.UsagePoint;

public class UsagePointInfo {
    private long id;
    private String info;
    private String mRID;

    public UsagePointInfo(UsagePoint up) {
        this.id = up.getId();
        this.info = up.getName();
        this.mRID = up.getMRID();
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

    public String getmRID() {
        return mRID;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }
}
