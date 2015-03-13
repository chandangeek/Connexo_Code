package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 30/10/2014
 * Time: 14:18
 */
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
