package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class DeviceFirmwareVersionInfo {
    public FirmwareTypeInfo firmwareType;
    public ActiveVersion activeVersion;
    public PendingVersion pendingVersion;

    public static class ActiveVersion {
        public String firmwareVersion;
        public FirmwareStatusInfo firmwareVersionStatus;
        public Long lastCheckedDate;

        public ActiveVersion() {}
    }

    public static class PendingVersion {
        public String firmwareVersion;
        public Long firmwareDeviceMessageId;
        public Long plannedDate;
        public UpgradeOptionInfo firmwareUpgradeOption;

        public PendingVersion() {}
    }

    public DeviceFirmwareVersionInfo() {}

    public DeviceFirmwareVersionInfo(FirmwareType firmwareType, Thesaurus thesaurus){
        this.firmwareType = new FirmwareTypeInfo(firmwareType, thesaurus);
    }

    public boolean hasTheSameType(FirmwareType candidate){
        return this.firmwareType != null && this.firmwareType.id != null && this.firmwareType.id.equals(candidate);
    }
}
