/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.rest.impl.CheckManagementOptionInfo;
import com.energyict.mdc.firmware.rest.impl.FirmwareTypeInfo;
import com.energyict.mdc.firmware.rest.impl.FirmwareVersionInfo;
import com.energyict.mdc.firmware.rest.impl.IdWithLocalizedValue;
import com.energyict.mdc.firmware.rest.impl.ManagementOptionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareCampaignInfo {
    public long id;
    public String name;
    public IdWithNameInfo status;
    public IdWithLocalizedValue<Long> deviceType;
    public String deviceGroup;
    public ManagementOptionInfo managementOption;
    public FirmwareTypeInfo firmwareType;
    public FirmwareVersionInfo firmwareVersion;
    public Instant startedOn;
    public Instant finishedOn;
    public Instant timeBoundaryStart;
    public Instant timeBoundaryEnd;
    public List<PropertyInfo> properties;
    public List<DevicesStatusAndQuantity> devices;
    public long version;
    public TimeDurationInfo validationTimeout;
    public IdWithNameInfo serviceCall;

    public IdWithNameInfo firmwareUploadComTask;
    public IdWithNameInfo firmwareUploadConnectionStrategy;

    public IdWithNameInfo validationComTask;
    public IdWithNameInfo validationConnectionStrategy;

    public EnumMap<FirmwareCheckManagementOption, CheckManagementOptionInfo> checkOptions;
    public boolean manuallyCancelled;
    public boolean withUniqueFirmwareVersion;

    public Optional<PropertyInfo> getPropertyInfo(String key) {
        return this.properties.stream().filter(y -> y.key.equals(key)).findFirst();
    }
}
