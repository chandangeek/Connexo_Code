/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Optional;

public class MasterHasLatestFirmwareCheck implements FirmwareCheck {
    private final FirmwareServiceImpl firmwareService;
    private final TopologyService topologyService;
    private final Thesaurus thesaurus;
    private final NoGhostFirmwareCheck noGhostFirmwareCheck;

    @Inject
    MasterHasLatestFirmwareCheck(FirmwareServiceImpl firmwareService, TopologyService topologyService, Thesaurus thesaurus,
                                 NoGhostFirmwareCheck noGhostFirmwareCheck) {
        this.firmwareService = firmwareService;
        this.topologyService = topologyService;
        this.thesaurus = thesaurus;
        this.noGhostFirmwareCheck = noGhostFirmwareCheck;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(FirmwareCheckTranslationKeys.MASTER_HAS_LATEST_FIRMWARE).format();
    }

    @Override
    public void execute(FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException {
        Device device = deviceUtils.getDevice();
        firmwareService.findFirmwareManagementOptions(device.getDeviceType()).ifPresent(firmwareManagementOptions -> {
            if (firmwareManagementOptions.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)) {
                topologyService.getPhysicalGateway(device)
                        .map(firmwareService::getFirmwareManagementDeviceUtilsFor)
                        .ifPresent(masterDeviceUtils -> {
                            Device master = masterDeviceUtils.getDevice();
                            if (noGhostFirmwareCheck.hasGhostMeterOrCommunicationFirmware(master)) {
                                throw new FirmwareCheckException(thesaurus, MessageSeeds.MASTER_HAS_GHOST_FIRMWARE);
                            }
                            if (!masterDeviceUtils.isReadOutAfterLastFirmwareUpgrade()) {
                                throw new FirmwareCheckException(thesaurus, MessageSeeds.MASTER_FIRMWARE_NOT_READOUT);
                            }
                            DeviceType masterDeviceType = master.getDeviceType();
                            EnumSet<FirmwareStatus> statusesForCheck = firmwareManagementOptions.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK);
                            EnumSet.of(FirmwareType.METER, FirmwareType.COMMUNICATION).stream()
                                    .filter(firmwareType -> firmwareService.isFirmwareTypeSupported(masterDeviceType, firmwareType))
                                    .forEach(firmwareType -> {
                                        Optional<FirmwareVersion> maximum = firmwareService.getMaximumFirmware(masterDeviceType, EnumSet.of(firmwareType), statusesForCheck);
                                        if (!maximum.isPresent() || !firmwareService.getActiveFirmwareVersion(master, firmwareType)
                                                .map(ActivatedFirmwareVersion::getFirmwareVersion)
                                                .filter(maximum.get()::equals)
                                                .isPresent()) {
                                            throw new FirmwareCheckException(thesaurus, MessageSeeds.MASTER_FIRMWARE_NOT_LATEST);
                                        }
                                    });
                        });
            }
        });
    }
}
