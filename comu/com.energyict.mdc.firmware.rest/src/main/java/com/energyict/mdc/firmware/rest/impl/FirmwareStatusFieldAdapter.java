package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareStatus;

public class FirmwareStatusFieldAdapter extends MapBasedXmlAdapter<FirmwareStatus> {

    public FirmwareStatusFieldAdapter() {
        register(MessageSeeds.Keys.STATUS_GHOST, FirmwareStatus.GHOST);
        register(MessageSeeds.Keys.STATUS_TEST, FirmwareStatus.TEST);
        register(MessageSeeds.Keys.STATUS_FINAL, FirmwareStatus.FINAL);
        register(MessageSeeds.Keys.STATUS_DEPRECATED, FirmwareStatus.DEPRECATED);
    }
}
