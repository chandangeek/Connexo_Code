package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceFirmwareVersionInfoFactory {
    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceFirmwareVersionInfoFactory(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, ResourceHelper resourceHelper) {
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.resourceHelper = resourceHelper;
    }

    public FirmwareAppender newInfo(){
        return new FirmwareAppender(this.thesaurus, this.deviceMessageSpecificationService, resourceHelper);
    }

    public static class FirmwareAppender {
        private final Thesaurus thesaurus;
        private final DeviceMessageSpecificationService deviceMessageSpecificationService;
        private final ResourceHelper resourceHelper;
        private final List<DeviceFirmwareVersionInfo> firmwares;

        private FirmwareAppender(Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, ResourceHelper resourceHelper) {
            this.thesaurus = thesaurus;
            this.deviceMessageSpecificationService = deviceMessageSpecificationService;
            this.resourceHelper = resourceHelper;
            this.firmwares = new ArrayList<>();
            this.firmwares.add(new DeviceFirmwareVersionInfo(FirmwareType.METER, this.thesaurus));
            this.firmwares.add(new DeviceFirmwareVersionInfo(FirmwareType.COMMUNICATION, this.thesaurus));
        }

        public FirmwareAppender addActive(ActivatedFirmwareVersion version){
            DeviceFirmwareVersionInfo.ActiveVersion activeVersion = new DeviceFirmwareVersionInfo.ActiveVersion();
            activeVersion.firmwareVersion = version.getFirmwareVersion().getFirmwareVersion();
            activeVersion.firmwareVersionStatus = new FirmwareStatusInfo(version.getFirmwareVersion().getFirmwareStatus(), thesaurus);
            activeVersion.lastCheckedDate = version.getLastChecked() != null ? version.getLastChecked().toEpochMilli() : null;

            this.firmwares.stream()
                    .filter(firmware -> firmware.hasTheSameType(version.getFirmwareVersion().getFirmwareType()))
                    .forEach(firmware -> firmware.activeVersion = activeVersion);
            return this;
        }

        public FirmwareAppender addPending(DeviceMessage<Device> message){
            Optional<FirmwareVersion> messageFirmwareVersion = resourceHelper.getFirmwareVersionFromMessage(message);
            if (messageFirmwareVersion.isPresent()){
                FirmwareVersion version = messageFirmwareVersion.get();
                DeviceFirmwareVersionInfo.PendingVersion pendingVersion = new DeviceFirmwareVersionInfo.PendingVersion();
                pendingVersion.firmwareVersion = version.getFirmwareVersion();
                pendingVersion.firmwareDeviceMessageId = message.getId();
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
