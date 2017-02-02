/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyFunctionTypeInfo {
    public long id;
    public String name;
    public String description;
    public KeyTypeInfo keyType;
    public TimeDurationInfo validityPeriod;
    public VersionInfo<String> parent;

    public KeyFunctionTypeInfo() {

    }

    public KeyFunctionTypeInfo(KeyAccessorType keyFunctionType, DeviceType deviceType) {
        this.id = keyFunctionType.getId();
        this.name = keyFunctionType.getName();
        this.description = keyFunctionType.getDescription();
        this.keyType = new KeyTypeInfo(keyFunctionType.getKeyType());
        if(keyFunctionType.getDuration().isPresent()) {
            this.validityPeriod = new TimeDurationInfo(keyFunctionType.getDuration().get());
        }
        this.parent = new VersionInfo<>(deviceType.getName(), deviceType.getVersion());
    }
}
