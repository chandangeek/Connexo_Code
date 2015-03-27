package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareType;

public class FirmwareTypeFieldAdapter extends MapBasedXmlAdapter<FirmwareType> {

    public FirmwareTypeFieldAdapter() {
        for (FirmwareType type : FirmwareType.values()) {
            register(type.getType(), type);
        }
    }
}
