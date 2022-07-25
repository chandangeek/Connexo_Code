/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class SetupFirmwareManagementCommand extends CommandWithTransaction {

    private final static String FIRMWARE_VERSION_V1 = "NTA-Sim_V_1.0.0";
    private final static String FIRMWARE_VERSION_V2 = "NTA-Sim_V_2.0.0";
    private final static String IMAGE_IDENTIFIER = "NTA-Sim_V_2.0.0";
    private final static String LANDIS_GYR_ZMD_DEVICETYPE = "Landis+Gyr ZMD";
    private final static String ACTARIS_SL7000_DEVICETYPE = "Actaris SL7000";

    private final FirmwareService firmwareService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public SetupFirmwareManagementCommand(FirmwareService firmwareService,
                                          DeviceConfigurationService deviceConfigurationService) {
        this.firmwareService = firmwareService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public void run() {
        deviceConfigurationService.findAllDeviceTypes().stream().forEach(this::setUpDeviceTypeForFirmwareManagement);
    }

    public static boolean isExcluded(DeviceType deviceType) {
        return deviceType.getName().equals(LANDIS_GYR_ZMD_DEVICETYPE);
    }

    private void setUpDeviceTypeForFirmwareManagement(DeviceType deviceType) {
        Set<ProtocolSupportedFirmwareOptions> supportedFirmwareOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);
        if (!isExcluded(deviceType) && !supportedFirmwareOptions.isEmpty()) {
            try (InputStream v1inputStream = getClass().getClassLoader().getResourceAsStream(FIRMWARE_VERSION_V1 + ".firm");
                 InputStream v2inputStream = getClass().getClassLoader().getResourceAsStream(FIRMWARE_VERSION_V2 + ".firm")) {
                FirmwareVersion v1 = firmwareService.getFirmwareVersionByVersionAndType(FIRMWARE_VERSION_V1, FirmwareType.METER, deviceType)
                        .orElseGet(() -> firmwareService.newFirmwareVersion(deviceType, FIRMWARE_VERSION_V1, FirmwareStatus.GHOST, FirmwareType.METER, IMAGE_IDENTIFIER).create());
                setFirmwareBytes(v1, v1inputStream);

                FirmwareVersion v2 = firmwareService.getFirmwareVersionByVersionAndType(FIRMWARE_VERSION_V2, FirmwareType.METER, deviceType)
                        .orElseGet(() -> firmwareService.newFirmwareVersion(deviceType, FIRMWARE_VERSION_V2, FirmwareStatus.GHOST, FirmwareType.METER, IMAGE_IDENTIFIER).create());
                setFirmwareBytes(v2, v2inputStream);
            } catch (IOException e) {
                throw new UnableToCreate("FirmwareFile could not be read");
            }
            if (firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType).isEmpty()) {
                FirmwareManagementOptions options = firmwareService.newFirmwareManagementOptions(deviceType);
                if (deviceType.getName().equals(ACTARIS_SL7000_DEVICETYPE)) {
                    options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK, Collections.singleton(FirmwareStatus.FINAL));
                }
                options.setOptions(supportedFirmwareOptions);
                options.save();
            }
        }
    }

    private void setFirmwareBytes(FirmwareVersion firmwareVersion, InputStream inputStream) {
        try {
            this.firmwareService.findAndLockFirmwareVersionByIdAndVersion(firmwareVersion.getId(), firmwareVersion.getVersion());
            firmwareVersion.setFirmwareFile(getBytes(inputStream));
            firmwareVersion.setFirmwareStatus(FirmwareStatus.FINAL);
            firmwareVersion.update();
        } catch (IOException exception) {
            throw new UnableToCreate("FirmwareFile " + firmwareVersion.getFirmwareVersion() + ".firm could not be read");
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return bytes;
    }
}
