/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String imageIdentifier;

    @JsonGetter
    @SuppressWarnings("unused")
    public String getImageIdentifier() {
        return imageIdentifier;
    }

    public void setImageIdentifier(String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
    }
}
