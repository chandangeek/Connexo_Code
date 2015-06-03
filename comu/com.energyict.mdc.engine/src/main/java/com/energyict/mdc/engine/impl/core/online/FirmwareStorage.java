package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.*;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides functionality to create/update ActivatedFirmwareVersions
 */
public class FirmwareStorage {

    private final ComServerDAOImpl.ServiceProvider serviceProvider;

    public FirmwareStorage(ComServerDAOImpl.ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    private FirmwareService getFirmwareService() {
        return this.serviceProvider.firmwareService();
    }

    void updateCommunicationFirmwareVersion(Optional<String> collectedCommunicationFirmwareVersion, Device device) {
        updateFirmwareVersionForType(collectedCommunicationFirmwareVersion, device, FirmwareType.COMMUNICATION);
    }

    void updateMeterFirmwareVersion(Optional<String> collectedMeterFirmwareVersion, Device device) {
        updateFirmwareVersionForType(collectedMeterFirmwareVersion, device, FirmwareType.METER);
    }

    private void updateFirmwareVersionForType(Optional<String> collectedFirmwareVersion, Device device, FirmwareType firmwareType) {
        collectedFirmwareVersion.ifPresent(version -> {
            if (!version.isEmpty()) {
                Optional<FirmwareVersion> existingFirmwareVersion = getFirmwareVersionFor(version, device.getDeviceType(), firmwareType);
                existingFirmwareVersion.map(firmwareVersion -> createOrUpdateActiveVersion(device, firmwareVersion)).
                        orElseGet(() -> createOrUpdateActiveVersion(device, createNewGhostFirmwareVersion(device, version, firmwareType)));
            }
        });
    }

    private FirmwareVersion createNewGhostFirmwareVersion(Device device, String version, FirmwareType firmwareType) {
        FirmwareVersion ghostVersion = getFirmwareService().newFirmwareVersion(device.getDeviceType(), version, FirmwareStatus.GHOST, firmwareType);
        getFirmwareService().saveFirmwareVersion(ghostVersion);
        return ghostVersion;
    }

    private ActivatedFirmwareVersion createOrUpdateActiveVersion(Device device, FirmwareVersion collectedFirmwareVersion) {
        Optional<ActivatedFirmwareVersion> activeFirmwareVersion = getCurrentActivatedFirmwareVersionFor(device, collectedFirmwareVersion.getFirmwareType());

        ActivatedFirmwareVersion activatedFirmwareVersion;
        if (!checkIfFirmwareVersionsAreEqual(collectedFirmwareVersion, activeFirmwareVersion)) {
            activatedFirmwareVersion = createNewActiveFirmwareVersion(device, collectedFirmwareVersion);
        } else {
            activatedFirmwareVersion = activeFirmwareVersion.get();
        }
        activatedFirmwareVersion.setLastChecked(now());
        getFirmwareService().saveActivatedFirmwareVersion(activatedFirmwareVersion);
        return activatedFirmwareVersion;
    }

    private Instant now() {
        return this.serviceProvider.clock().instant();
    }

    private ActivatedFirmwareVersion createNewActiveFirmwareVersion(Device device, FirmwareVersion collectedFirmwareVersion) {
        return getFirmwareService().newActivatedFirmwareVersionFrom(device, collectedFirmwareVersion, getIntervalFromNow());
    }

    private Boolean checkIfFirmwareVersionsAreEqual(FirmwareVersion firmwareVersion, Optional<ActivatedFirmwareVersion> activeFirmwareVersion) {
        return activeFirmwareVersion
                .map(activatedFirmwareVersion -> activatedFirmwareVersion.getFirmwareVersion().getFirmwareVersion().equals(firmwareVersion.getFirmwareVersion()))
                .orElse(false);
    }

    private Optional<ActivatedFirmwareVersion> getCurrentActivatedFirmwareVersionFor(Device device, FirmwareType firmwareType) {
        Optional<ActivatedFirmwareVersion> activeFirmwareVersion = Optional.empty();
        if (firmwareType.equals(FirmwareType.METER)) {
            activeFirmwareVersion = getFirmwareService().getCurrentMeterFirmwareVersionFor(device);
        } else if (firmwareType.equals(FirmwareType.COMMUNICATION)) {
            activeFirmwareVersion = getFirmwareService().getCurrentCommunicationFirmwareVersionFor(device);
        }
        return activeFirmwareVersion;
    }

    private Interval getIntervalFromNow() {
        return Interval.of(Range.atLeast(now()));
    }

    private Optional<FirmwareVersion> getFirmwareVersionFor(String collectedVersion, DeviceType deviceType, FirmwareType firmwareType) {
        return getFirmwareService().getFirmwareVersionByVersionAndType(collectedVersion, firmwareType, deviceType);
    }

}