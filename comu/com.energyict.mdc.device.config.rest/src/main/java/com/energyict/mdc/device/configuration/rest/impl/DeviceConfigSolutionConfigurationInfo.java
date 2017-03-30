/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfigSolutionConfigurationInfo {

    public String action;
    public DeviceConfigConfigurationInfo to;
    public DeviceConfigConfigurationInfo from;

    public DeviceConfigSolutionConfigurationInfo() {
    }

    public DeviceConfigSolutionConfigurationInfo(String action,
                                                 DeviceConfigConfigurationInfo from) {
        this.action = action;
        this.from = from;
    }

    public DeviceConfigSolutionConfigurationInfo(String action,
                                                 DeviceConfigConfigurationInfo from,
                                                 DeviceConfigConfigurationInfo to) {
        this.action = action;
        this.from = from;
        this.to = to;
    }
}