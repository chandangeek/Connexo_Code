/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceFirmwareHistoryInfosFactory {

    private static final String NO_VALUE = "-";
    private final FirmwareService firmwareService;
    private FirmwareManagementDeviceUtils versionUtils;

    @Inject
    public DeviceFirmwareHistoryInfosFactory(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    public List<DeviceFirmwareHistoryInfos> from(Device device) {
        versionUtils = firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        List<DeviceMessage> deviceMessageList = device.getMessages();
        List<DeviceFirmwareHistoryInfos> firmwareVersionList = new ArrayList<>();
        if (!deviceMessageList.isEmpty()) {
            firmwareVersionList = getDeviceFirmwareHistoryInfos(deviceMessageList);
        }
        List<DeviceFirmwareHistoryInfos> deviceFirmwareHistoryInfosListSorted = firmwareVersionList.stream()
                .sorted(Comparator.comparing(DeviceFirmwareHistoryInfos::getUploadedOn).reversed())
                .collect(Collectors.toList());
        return deviceFirmwareHistoryInfosListSorted;
    }

    private List<DeviceFirmwareHistoryInfos> getDeviceFirmwareHistoryInfos(List<DeviceMessage> deviceMessageList) {
        List<DeviceFirmwareHistoryInfos> firmwareVersionList = new ArrayList<>();
        for (DeviceMessage deviceMessage : deviceMessageList) {
            DeviceFirmwareHistoryInfos deviceFirmwareHistoryInfos = new DeviceFirmwareHistoryInfos();
            deviceFirmwareHistoryInfos.setUploadedOn(deviceMessage.getCreationDate());
            deviceFirmwareHistoryInfos.setResult(deviceMessage.getStatus());
            deviceFirmwareHistoryInfos.setTriggerdBy(deviceMessage.getUser());
            Optional<FirmwareVersion> firmwareVersionFromMessage = versionUtils.getFirmwareVersionFromMessage(deviceMessage);
            deviceFirmwareHistoryInfos.setVersion(firmwareVersionFromMessage.isPresent() ? firmwareVersionFromMessage.get().getFirmwareVersion() : NO_VALUE);
            Optional<Instant> activationDateFromMessage = versionUtils.getActivationDateFromMessage(deviceMessage);
            deviceFirmwareHistoryInfos.setActivationDate(activationDateFromMessage.isPresent() ? activationDateFromMessage.get() : null);
            firmwareVersionList.add(deviceFirmwareHistoryInfos);
        }
        return firmwareVersionList;
    }
}
