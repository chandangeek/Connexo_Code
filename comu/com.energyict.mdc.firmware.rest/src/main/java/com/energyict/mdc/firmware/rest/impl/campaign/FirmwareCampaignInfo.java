/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.firmware.rest.impl.FirmwareTypeInfo;
import com.energyict.mdc.firmware.rest.impl.FirmwareVersionInfo;
import com.energyict.mdc.firmware.rest.impl.IdWithLocalizedValue;
import com.energyict.mdc.firmware.rest.impl.ManagementOptionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareCampaignInfo {
    public long id;
    public String name;
    public String status;
    public IdWithLocalizedValue<Long> deviceType;
    public String deviceGroup;
    public ManagementOptionInfo managementOption;
    public FirmwareTypeInfo firmwareType;
    public FirmwareVersionInfo firmwareVersion;
    public Instant startedOn;
    public Instant finishedOn;
    public Instant timeBoundaryStart;
    public Instant timeBoundaryEnd;
    public Instant activationDate;
    public List<PropertyInfo> properties;
    public List<DevicesStatusAndQuantity> devices;
    public long version;
    public long validationTimeout;

    public Optional<PropertyInfo> getPropertyInfo(String key) {
        return this.properties.stream().filter(y -> y.key.equals(key)).findFirst();
    }
}
