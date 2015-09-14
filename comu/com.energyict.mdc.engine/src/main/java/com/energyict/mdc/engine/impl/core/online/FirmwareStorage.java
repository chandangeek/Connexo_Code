package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.*;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides functionality to create/update {@link ActivatedFirmwareVersion}s.
 */
public class FirmwareStorage {

    private final FirmwareService firmwareService;
    private final Clock clock;

    public FirmwareStorage(FirmwareService firmwareService, Clock clock) {
        this.firmwareService = firmwareService;
        this.clock = clock;
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
        ghostVersion.save();
        return ghostVersion;
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