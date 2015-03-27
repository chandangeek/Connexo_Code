package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareType;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class FirmwareTypeInfo {
    private static final FirmwareTypeFieldAdapter FIRMWARE_TYPE_ADAPTER = new FirmwareTypeFieldAdapter();
    @XmlJavaTypeAdapter(FirmwareTypeAdapter.class)
    public FirmwareType id;
    public String displayValue;

    public FirmwareTypeInfo() {
    }

    public FirmwareTypeInfo(FirmwareType firmwareType, Thesaurus thesaurus) {
        this.id = firmwareType;
        this.displayValue = thesaurus.getString(FIRMWARE_TYPE_ADAPTER.marshal(firmwareType), FIRMWARE_TYPE_ADAPTER.marshal(firmwareType));
    }
}
