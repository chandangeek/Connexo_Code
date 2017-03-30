/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareType;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class FirmwareTypeInfo {
    static final FirmwareTypeFieldAdapter FIRMWARE_TYPE_ADAPTER = new FirmwareTypeFieldAdapter();

    @XmlJavaTypeAdapter(FirmwareTypeFieldAdapter.class)
    public FirmwareType id;
    public String localizedValue;

    public FirmwareTypeInfo() {
    }

    public FirmwareTypeInfo(FirmwareType firmwareType, Thesaurus thesaurus) {
        this.id = firmwareType;
        this.localizedValue = thesaurus.getString(FIRMWARE_TYPE_ADAPTER.marshal(firmwareType), FIRMWARE_TYPE_ADAPTER.marshal(firmwareType));
    }
}
