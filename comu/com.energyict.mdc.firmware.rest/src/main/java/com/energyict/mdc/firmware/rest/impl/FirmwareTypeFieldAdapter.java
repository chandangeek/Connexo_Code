/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareType;

public class FirmwareTypeFieldAdapter extends MapBasedXmlAdapter<FirmwareType> {

    static final FirmwareTypeFieldAdapter INSTANCE = new FirmwareTypeFieldAdapter();

    public FirmwareTypeFieldAdapter() {
        register(FirmwareType.METER.getType(), FirmwareType.METER);
        register(FirmwareType.COMMUNICATION.getType(), FirmwareType.COMMUNICATION);
    }
}
