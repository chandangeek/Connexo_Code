package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareType;

public class FirmwareTypeFieldAdapter extends MapBasedXmlAdapter<FirmwareType> {

    public FirmwareTypeFieldAdapter() {
        register(MessageSeeds.Keys.TYPE_METER, FirmwareType.METER);
        register(MessageSeeds.Keys.TYPE_COMMUNICATION, FirmwareType.COMMUNICATION);
    }

    public FirmwareTypeFieldAdapter(boolean needSupportCommunicationFirmware) {
        register(MessageSeeds.Keys.TYPE_METER, FirmwareType.METER);
        if (needSupportCommunicationFirmware) {
            register(MessageSeeds.Keys.TYPE_COMMUNICATION, FirmwareType.COMMUNICATION);
        }
    }
}
