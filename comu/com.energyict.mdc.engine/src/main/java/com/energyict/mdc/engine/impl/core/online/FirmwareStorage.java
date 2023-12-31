/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides functionality to create/update {@link ActivatedFirmwareVersion}s.
 */
class FirmwareStorage {

    private final FirmwareService firmwareService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Clock clock;

    FirmwareStorage(FirmwareService firmwareService, Clock clock, DeviceConfigurationService deviceConfigurationService) {
        this.firmwareService = firmwareService;
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    private FirmwareService getFirmwareService() {
        return this.firmwareService;
    }

    void updateCommunicationFirmwareVersion(Optional<String> collectedCommunicationFirmwareVersion, Device device) {
        updateFirmwareVersionForType(collectedCommunicationFirmwareVersion, device, FirmwareType.COMMUNICATION);
    }

    void updateMeterFirmwareVersion(Optional<String> collectedMeterFirmwareVersion, Device device) {
        updateFirmwareVersionForType(collectedMeterFirmwareVersion, device, FirmwareType.METER);
    }

    void updateCaConfigImageVersion(Optional<String> collectedCaConfigImageVersion, Device device) {
        updateFirmwareVersionForType(collectedCaConfigImageVersion, device, FirmwareType.CA_CONFIG_IMAGE);
    }

    void updateAuxiliaryFirmwareVersion(Optional<String> collectedAuxiliaryFirmwareVersion, Device device) {
        updateFirmwareVersionForType(collectedAuxiliaryFirmwareVersion, device, FirmwareType.AUXILIARY);
    }

    private void updateFirmwareVersionForType(Optional<String> collectedFirmwareVersion, Device device, FirmwareType firmwareType) {
        collectedFirmwareVersion.ifPresent(version -> {
            if (!version.isEmpty()) {
                FirmwareVersion firmwareVersion;
                Optional<FirmwareVersion> existentFirmwareVersion = getFirmwareVersionFor(version, device.getDeviceType(), firmwareType);
                if (existentFirmwareVersion.isPresent()) {
                    firmwareVersion = existentFirmwareVersion.get();
                } else {
                    deviceConfigurationService.findAndLockDeviceType(device.getDeviceType().getId());
                    firmwareVersion = getFirmwareVersionFor(version, device.getDeviceType(), firmwareType)
                            .orElseGet(() -> createNewGhostFirmwareVersion(device, version, firmwareType));
                }
                createOrUpdateActiveVersion(device, firmwareVersion);
            }
        });
    }

    private FirmwareVersion createNewGhostFirmwareVersion(Device device, String version, FirmwareType firmwareType) {
        return getFirmwareService().newFirmwareVersion(device.getDeviceType(), version, FirmwareStatus.GHOST, firmwareType, version).create();
    }

    private ActivatedFirmwareVersion createOrUpdateActiveVersion(Device device, FirmwareVersion collectedFirmwareVersion) {
        Optional<ActivatedFirmwareVersion> activeFirmwareVersion = getFirmwareService().getActiveFirmwareVersion(device, collectedFirmwareVersion.getFirmwareType());

        ActivatedFirmwareVersion activatedFirmwareVersion;
        if (!checkIfFirmwareVersionsAreEqual(collectedFirmwareVersion, activeFirmwareVersion)) {
            activatedFirmwareVersion = createNewActiveFirmwareVersion(device, collectedFirmwareVersion);
        } else {
            activatedFirmwareVersion = activeFirmwareVersion.get();
        }
        activatedFirmwareVersion.setLastChecked(now());
        activatedFirmwareVersion.save();
        return activatedFirmwareVersion;
    }

    private Instant now() {
        return this.clock.instant();
    }

    private ActivatedFirmwareVersion createNewActiveFirmwareVersion(Device device, FirmwareVersion collectedFirmwareVersion) {
        return getFirmwareService().newActivatedFirmwareVersionFrom(device, collectedFirmwareVersion, getIntervalFromNow());
    }

    private Boolean checkIfFirmwareVersionsAreEqual(FirmwareVersion firmwareVersion, Optional<ActivatedFirmwareVersion> activeFirmwareVersion) {
        return activeFirmwareVersion
                .map(activatedFirmwareVersion -> activatedFirmwareVersion.getFirmwareVersion().getFirmwareVersion().equals(firmwareVersion.getFirmwareVersion()))
                .orElse(false);
    }

    private Interval getIntervalFromNow() {
        return Interval.of(Range.atLeast(now()));
    }

    private Optional<FirmwareVersion> getFirmwareVersionFor(String collectedVersion, DeviceType deviceType, FirmwareType firmwareType) {
        return getFirmwareService().getFirmwareVersionByVersionAndType(collectedVersion, firmwareType, deviceType);
    }

}