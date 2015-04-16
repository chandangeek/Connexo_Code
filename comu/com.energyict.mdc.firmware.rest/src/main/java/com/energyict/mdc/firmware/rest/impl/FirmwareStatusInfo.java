package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareStatus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class FirmwareStatusInfo {
    private static final FirmwareStatusFieldAdapter FIRMWARE_STATUS_ADAPTER = new FirmwareStatusFieldAdapter();
    @XmlJavaTypeAdapter(FirmwareStatusFieldAdapter.class)
    public FirmwareStatus id;
    public String localizedValue;

    public FirmwareStatusInfo() {
    }

    public FirmwareStatusInfo(FirmwareStatus firmwareStatus, Thesaurus thesaurus) {
        this.id = firmwareStatus;
        this.localizedValue = thesaurus.getString(FIRMWARE_STATUS_ADAPTER.marshal(firmwareStatus), FIRMWARE_STATUS_ADAPTER.marshal(firmwareStatus));
    }
}
