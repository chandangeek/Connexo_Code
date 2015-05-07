package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 07/05/15
 * Time: 13:49
 */
public class FirmwareTypeInfos {

    private List<FirmwareTypeInfo> firmwareTypes;

    public FirmwareTypeInfos(DeviceType deviceType, Thesaurus thesaurus) {
        this.firmwareTypes = new ArrayList<>();
        this.firmwareTypes.add(new FirmwareTypeInfo(FirmwareType.METER, thesaurus));
        if (deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().supportsCommunicationFirmwareVersion()) {
            this.firmwareTypes.add(new FirmwareTypeInfo(FirmwareType.COMMUNICATION, thesaurus));
        }
    }

    @JsonProperty("firmwareTypes")
    @SuppressWarnings("unused")
    public List<FirmwareTypeInfo> getFirmwareTypes() {
        return firmwareTypes;
    }
}
