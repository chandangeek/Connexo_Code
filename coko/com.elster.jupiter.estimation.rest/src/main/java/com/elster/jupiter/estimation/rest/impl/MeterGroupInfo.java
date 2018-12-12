/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MeterGroupInfo {

    public long id;
    public String name;

    public MeterGroupInfo() {
    }

    public MeterGroupInfo(EndDeviceGroup group) {
        id = group.getId();
        name = group.getName();
    }
}
