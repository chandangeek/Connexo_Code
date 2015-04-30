package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.commands.store.DeviceFirmwareVersionStorageTransitions;
import com.energyict.mdc.engine.impl.commands.store.FirmwareVersionStorageTransition;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
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
        collectedCommunicationFirmwareVersion.ifPresent(version -> {
            if (!version.equals(FirmwareVersionStorageTransition.Constants.EMPTY)) {
                Optional<FirmwareVersion> existingFirmwareVersion = getFirmwareVersionFor(version, device.getDeviceType());
                existingFirmwareVersion.map(firmwareVersion -> createOrUpdateActiveVersion(device, firmwareVersion)).
                        orElseGet(() -> createOrUpdateActiveVersion(device, createNewGhostFirmwareVersion(device, version, FirmwareType.COMMUNICATION)));
            }
        });
    }

    void updateMeterFirmwareVersion(Optional<String> collectedMeterFirmwareVersion, Device device) {
        collectedMeterFirmwareVersion.ifPresent(version -> {
            if (!version.equals(FirmwareVersionStorageTransition.Constants.EMPTY)) {
                Optional<FirmwareVersion> existingFirmwareVersion = getFirmwareVersionFor(version, device.getDeviceType());
                existingFirmwareVersion.map(firmwareVersion -> createOrUpdateActiveVersion(device, firmwareVersion)).
                        orElseGet(() -> createOrUpdateActiveVersion(device, createNewGhostFirmwareVersion(device, version, FirmwareType.METER)));
            }
        });
    }

    FirmwareVersion createNewGhostFirmwareVersion(Device device, String version, FirmwareType firmwareType) {
        FirmwareVersion ghostVersion = getFirmwareService().newFirmwareVersion(device.getDeviceType(), version, FirmwareStatus.GHOST, firmwareType);
        getFirmwareService().saveFirmwareVersion(ghostVersion);
        return ghostVersion;
    }

    ActivatedFirmwareVersion createOrUpdateActiveVersion(Device device, FirmwareVersion collectedFirmwareVersion) {
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

    ActivatedFirmwareVersion createNewActiveFirmwareVersion(Device device, FirmwareVersion collectedFirmwareVersion) {
        return getFirmwareService().newActivatedFirmwareVersionFrom(device, collectedFirmwareVersion, getIntervalFromNow());
    }

    Boolean checkIfFirmwareVersionsAreEqual(FirmwareVersion firmwareVersion, Optional<ActivatedFirmwareVersion> activeFirmwareVersion) {
        return activeFirmwareVersion
                .map(activatedFirmwareVersion -> activatedFirmwareVersion.getFirmwareVersion().getFirmwareVersion().equals(firmwareVersion.getFirmwareVersion()))
                .orElse(false);
    }

    Optional<ActivatedFirmwareVersion> getCurrentActivatedFirmwareVersionFor(Device device, FirmwareType firmwareType) {
        Optional<ActivatedFirmwareVersion> activeFirmwareVersion = Optional.empty();
        if (firmwareType.equals(FirmwareType.METER)) {
            activeFirmwareVersion = getFirmwareService().getCurrentMeterFirmwareVersionFor(device);
        } else if (firmwareType.equals(FirmwareType.COMMUNICATION)) {
            activeFirmwareVersion = getFirmwareService().getCurrentCommunicationFirmwareVersionFor(device);
        }
        return activeFirmwareVersion;
    }

    Interval getIntervalFromNow() {
        return Interval.of(Range.atLeast(now()));
    }

    void defineCommunicationFirmwareVersionTransition(CollectedFirmwareVersion collectedFirmwareVersions, DeviceFirmwareVersionStorageTransitions deviceFirmwareVersionStorageTransitions, Device device) {
        String currentCommunicationFirmwareVersionStatus = getCurrentCommunicationFirmwareVersionStatus(device);
        String collectedMeterFirmwareVersionStatus = collectedFirmwareVersions.getActiveMeterFirmwareVersion().map(version -> getFirmwareStatus(device, version)).orElse(FirmwareVersionStorageTransition.Constants.EMPTY);
        deviceFirmwareVersionStorageTransitions.setActiveCommunicationFirmwareVersionTransition(FirmwareVersionStorageTransition.from(currentCommunicationFirmwareVersionStatus, collectedMeterFirmwareVersionStatus));

    }

    String getCurrentCommunicationFirmwareVersionStatus(Device device) {
        Optional<ActivatedFirmwareVersion> currentCommunicationFirmwareVersionFor = getFirmwareService().getCurrentCommunicationFirmwareVersionFor(device);
        return currentCommunicationFirmwareVersionFor.map(activatedFirmwareVersion -> activatedFirmwareVersion.getFirmwareVersion().getFirmwareStatus().getStatus())
                .orElseGet(() -> FirmwareVersionStorageTransition.Constants.EMPTY);
    }

    void defineMeterFirmwareVersionTransition(CollectedFirmwareVersion collectedFirmwareVersions, DeviceFirmwareVersionStorageTransitions deviceFirmwareVersionStorageTransitions, Device device) {
        String currentMeterFirmwareVersionStatus = getCurrentMeterFirmwareVersionStatus(device);
        String collectedMeterFirmwareVersionStatus = collectedFirmwareVersions.getActiveMeterFirmwareVersion().map(version -> getFirmwareStatus(device, version)).orElse(FirmwareVersionStorageTransition.Constants.EMPTY);
        deviceFirmwareVersionStorageTransitions.setActiveMeterFirmwareVersionTransition(FirmwareVersionStorageTransition.from(currentMeterFirmwareVersionStatus, collectedMeterFirmwareVersionStatus));
    }

    String getCurrentMeterFirmwareVersionStatus(Device device) {
        Optional<ActivatedFirmwareVersion> currentMeterFirmwareVersionFor = getFirmwareService().getCurrentMeterFirmwareVersionFor(device);
        return currentMeterFirmwareVersionFor.map(activatedFirmwareVersion -> activatedFirmwareVersion.getFirmwareVersion().getFirmwareStatus().getStatus())
                .orElseGet(() -> FirmwareVersionStorageTransition.Constants.EMPTY);
    }

    String getFirmwareStatus(Device device, String collectedVersion) {
        Optional<FirmwareVersion> meterFirmwareVersionOnDeviceType = getFirmwareVersionFor(collectedVersion, device.getDeviceType());
        return getFirmwareStatusFromOptionalVersion(meterFirmwareVersionOnDeviceType);
    }

    Optional<FirmwareVersion> getFirmwareVersionFor(String collectedVersion, DeviceType deviceType) {
        return getFirmwareService().getFirmwareVersionByVersion(collectedVersion, deviceType);
    }

    String getFirmwareStatusFromOptionalVersion(Optional<FirmwareVersion> firmwareVersionOnDeviceType) {
        return firmwareVersionOnDeviceType.map(firmwareVersion -> firmwareVersion.getFirmwareStatus().getStatus()).orElse(FirmwareVersionStorageTransition.Constants.EMPTY);
    }
}