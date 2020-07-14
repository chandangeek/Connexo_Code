/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean;

import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;

public class UsagePointInfo {

    private String mrid;
    private String name;

    public UsagePointInfo() {
        super();
    }

    public UsagePointInfo(UsagePoint usagePoint) {
        super();
        setMrid(usagePoint.getMRID());
        if (!usagePoint.getNames().isEmpty()) {
            setName(usagePoint.getNames().get(0).getName());
        }
    }

    public String getMrid() {
        return mrid;
    }

    public void setMrid(String mrid) {
        this.mrid = mrid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
