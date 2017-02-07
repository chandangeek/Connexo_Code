/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.LogBookSpec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogBookSpecInfo extends LogBookTypeInfo{
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public long version;
    public VersionInfo<Long> parent;

    public static LogBookTypeInfo from(LogBookSpec logBookSpec) {
        LogBookSpecInfo info = new LogBookSpecInfo();
        info.id = logBookSpec.getId();
        info.name = logBookSpec.getLogBookType().getName();
        info.obisCode = logBookSpec.getObisCode();
        info.overruledObisCode = logBookSpec.getDeviceObisCode();
        info.version = logBookSpec.getVersion();
        info.parent = new VersionInfo<>(logBookSpec.getDeviceConfiguration().getId(), logBookSpec.getDeviceConfiguration().getVersion());
        return info;
    }
}
