/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareStatus;

public class FirmwareStatusFieldAdapter extends MapBasedXmlAdapter<FirmwareStatus> {

    static final FirmwareStatusFieldAdapter INSTANCE = new FirmwareStatusFieldAdapter();

    public FirmwareStatusFieldAdapter() {
        register(FirmwareStatus.GHOST.getStatus(), FirmwareStatus.GHOST);
        register(FirmwareStatus.TEST.getStatus(), FirmwareStatus.TEST);
        register(FirmwareStatus.FINAL.getStatus(), FirmwareStatus.FINAL);
        register(FirmwareStatus.DEPRECATED.getStatus(), FirmwareStatus.DEPRECATED);
    }
}
