/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfigConfigurationInfo {

    public long id;
    public String name;

    public DeviceConfigConfigurationInfo() {
    }

    public DeviceConfigConfigurationInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }
}