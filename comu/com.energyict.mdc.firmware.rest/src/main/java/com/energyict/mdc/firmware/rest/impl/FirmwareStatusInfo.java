/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.firmware.FirmwareStatus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class FirmwareStatusInfo {
    @XmlJavaTypeAdapter(FirmwareStatusFieldAdapter.class)
    public FirmwareStatus id;
    public String localizedValue;

    public FirmwareStatusInfo() {
    }

    public FirmwareStatusInfo(FirmwareStatus firmwareStatus, String localizedValue) {
        this.id = firmwareStatus;
        this.localizedValue = localizedValue;
    }
}
