/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean;

import ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice;

public class EndDeviceInfo {
    private String mrid;
    private String name;

    public EndDeviceInfo() {
        super();
    }

    public EndDeviceInfo(EndDevice endDevice) {
        super();
        setMrid(endDevice.getMRID());
        if (!endDevice.getNames().isEmpty()) {
            setName(endDevice.getNames().get(0).getName());
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
