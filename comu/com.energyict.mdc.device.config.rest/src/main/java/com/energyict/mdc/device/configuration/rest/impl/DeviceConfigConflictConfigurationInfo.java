/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfigConflictConfigurationInfo {

    public DeviceConfigConfigurationInfo from;
    public List<DeviceConfigConfigurationInfo> to;

    public DeviceConfigConflictConfigurationInfo() {
    }

    public DeviceConfigConflictConfigurationInfo(DeviceConfigConfigurationInfo from,
                                                 List<DeviceConfigConfigurationInfo> to) {
        this.from = from;
        this.to = to;
    }
}