package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceFirmwareVersionInfoFactory {
    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public DeviceFirmwareVersionInfoFactory(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    public FirmwareAppender newInfo(){
        return new FirmwareAppender(this.thesaurus, this.deviceMessageSpecificationService);
    }

    public static class FirmwareAppender {
        private final Thesaurus thesaurus;
        private final DeviceMessageSpecificationService deviceMessageSpecificationService;
        private final List<DeviceFirmwareVersionInfo> firmwares;

        private FirmwareAppender(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService) {
            this.thesaurus = thesaurus;
            this.deviceMessageSpecificationService = deviceMessageSpecificationService;
            this.firmwares = new ArrayList<>();
            this.firmwares.add(new DeviceFirmwareVersionInfo(FirmwareType.METER, this.thesaurus));
            this.firmwares.add(new DeviceFirmwareVersionInfo(FirmwareType.COMMUNICATION, this.thesaurus));
        }

        public FirmwareAppender addVersion(ActivatedFirmwareVersion version){
            DeviceFirmwareVersionInfo.ActiveVersion activeVersion = new DeviceFirmwareVersionInfo.ActiveVersion();
            activeVersion.firmwareVersion = version.getFirmwareVersion().getFirmwareVersion();
            activeVersion.firmwareVersionStatus = new FirmwareStatusInfo(version.getFirmwareVersion().getFirmwareStatus(), thesaurus);
            activeVersion.lastCheckedDate = version.getLastChecked() != null ? version.getLastChecked().toEpochMilli() : null;

            this.firmwares.stream()
                    .filter(firmware -> firmware.hasTheSameType(version.getFirmwareVersion().getFirmwareType()))
                    .forEach(firmware -> firmware.activeVersion = activeVersion);
            return this;
        }

        public FirmwareAppender addVersion(DeviceMessage<Device> message){
            Optional<DeviceMessageAttribute> messageFirmwareVersion = message.getAttributes()
                    .stream()
                    .filter(attr -> DeviceMessageConstants.firmwareUpdateFileAttributeName.equals(attr.getName()))
                    .findFirst();
            if (messageFirmwareVersion.isPresent()){
                FirmwareVersion version = (FirmwareVersion) messageFirmwareVersion.get().getValue();
                DeviceFirmwareVersionInfo.PendingVersion pendingVersion = new DeviceFirmwareVersionInfo.PendingVersion();
                pendingVersion.firmwareVersion = version.getFirmwareVersion();
                pendingVersion.firmwareDeviceMessageId = message.getDeviceMessageId().dbValue();
                pendingVersion.plannedDate = message.getReleaseDate() != null ? message.getReleaseDate().toEpochMilli() : null;
                Optional<ProtocolSupportedFirmwareOptions> upgradeOptionRef = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(message.getDeviceMessageId());
                upgradeOptionRef.ifPresent(upgradeOption -> {
                    pendingVersion.firmwareUpgradeOption = new UpgradeOptionInfo(upgradeOption.getId(), thesaurus.getString(upgradeOption.getId(), upgradeOption.getId()));
                });
                this.firmwares.stream()
                        .filter(firmware -> firmware.hasTheSameType(version.getFirmwareType()))
                        .forEach(firmware -> firmware.pendingVersion = pendingVersion);
            }
            return this;
        }

        public List<DeviceFirmwareVersionInfo> getFirmwares() {
            return firmwares;
        }
    }
}
