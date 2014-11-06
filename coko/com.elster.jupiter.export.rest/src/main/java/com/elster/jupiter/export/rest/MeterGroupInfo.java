package com.elster.jupiter.export.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

/**
 * Copyrights EnergyICT
 * Date: 30/10/2014
 * Time: 14:18
 */
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
