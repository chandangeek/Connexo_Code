package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareStatus;

public class FirmwareStatusFieldAdapter extends MapBasedXmlAdapter<FirmwareStatus> {

    public FirmwareStatusFieldAdapter() {
        for (FirmwareStatus status : FirmwareStatus.values()) {
            register(status.getStatus(), status);
        }
    }
}
