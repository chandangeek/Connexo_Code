/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareVersionInfo {
    public Long id;
    public String firmwareVersion;
    public FirmwareTypeInfo firmwareType;
    public FirmwareStatusInfo firmwareStatus;
    public Integer fileSize;
    public Boolean isInUse;
    public long version;
    public String imageIdentifier;
    public int rank;
    public IdWithNameInfo meterFirmwareDependency;
    public IdWithNameInfo communicationFirmwareDependency;
    public IdWithNameInfo auxiliaryFirmwareDependency;

    public int getRank() {
        return rank;
    }
}
