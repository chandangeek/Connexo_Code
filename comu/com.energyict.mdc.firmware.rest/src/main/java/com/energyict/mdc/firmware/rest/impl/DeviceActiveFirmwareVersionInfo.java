package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersion;

public class DeviceActiveFirmwareVersionInfo {
    public FirmwareTypeInfo firmwareType;
    public ActiveVersion activeVersion;
    public PendingVersion pendingVersion;

    class ActiveVersion {
        public String firmwareVersion;
        public FirmwareStatusInfo firmwareVersionStatus;
        public Long lastCheckedDate;

        public ActiveVersion() {

        }
    }

    class PendingVersion {
        public Long firmwareDeviceMessageId;
        public String firmwareVersion;
        public Long plannedDate;

        public PendingVersion() {

        }
    }

    public DeviceActiveFirmwareVersionInfo() {

    }

    public DeviceActiveFirmwareVersionInfo(ActivatedFirmwareVersion activatedFirmwareVersion, /*DeviceMessage<> deviceMessage,*/ Thesaurus thesaurus) {
        FirmwareVersion firmwareVersion = activatedFirmwareVersion.getFirmwareVersion();
        this.firmwareType = new FirmwareTypeInfo(firmwareVersion.getFirmwareType(), thesaurus);
        this.activeVersion = new ActiveVersion();
        this.activeVersion.firmwareVersion = firmwareVersion.getFirmwareVersion();
        this.activeVersion.firmwareVersionStatus = new FirmwareStatusInfo(firmwareVersion.getFirmwareStatus(), thesaurus);
        this.activeVersion.lastCheckedDate = activatedFirmwareVersion.getLastChecked() != null ? activatedFirmwareVersion.getLastChecked().toEpochMilli() : null;
/*        if (deviceMessage != null) {
            this.pendingVersion = new PendingVersion();
            this.pendingVersion.firmwareDeviceMessageId = deviceMessage.getId();
            this.pendingVersion.firmwareVersion = ""; // TODO
            this.pendingVersion.plannedDate = deviceMessage.getReleaseDate().toEpochMilli(); // TODO verify
        }*/
    }
}
