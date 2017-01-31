/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareType;

import java.util.ArrayList;
import java.util.List;

public class FirmwareTypeInfos {

    public List<FirmwareTypeInfo> firmwareTypes;

    public FirmwareTypeInfos(DeviceType deviceType, Thesaurus thesaurus) {
        this.firmwareTypes = new ArrayList<>();
        this.firmwareTypes.add(new FirmwareTypeInfo(FirmwareType.METER, thesaurus));
        if (deviceType.getDeviceProtocolPluggableClass().isPresent() && deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().supportsCommunicationFirmwareVersion()) {
            this.firmwareTypes.add(new FirmwareTypeInfo(FirmwareType.COMMUNICATION, thesaurus));
        }
    }
}
