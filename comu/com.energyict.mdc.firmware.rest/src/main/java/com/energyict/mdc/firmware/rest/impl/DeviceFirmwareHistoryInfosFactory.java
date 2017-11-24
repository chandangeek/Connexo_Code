/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceFirmwareHistoryInfosFactory {

    private final FirmwareService firmwareService;
    private Thesaurus thesaurus;

    @Inject
    public DeviceFirmwareHistoryInfosFactory(FirmwareService firmwareService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    public List<DeviceFirmwareHistoryInfos> from(Device device) {
        FirmwareManagementDeviceUtils versionUtils = firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        List<DeviceMessage> deviceMessageList = device.getMessages();
        List<DeviceFirmwareHistoryInfos> firmwareVersionList = new ArrayList<>();
        if (!deviceMessageList.isEmpty()) {
            firmwareVersionList = getDeviceFirmwareHistoryInfos(deviceMessageList, versionUtils);
        }
        return sortDescendingByUploadedOnTimestampDeviceFirmwareHistoryInfos(firmwareVersionList);
    }

    private List<DeviceFirmwareHistoryInfos> getDeviceFirmwareHistoryInfos(List<DeviceMessage> deviceMessageList, FirmwareManagementDeviceUtils versionUtils) {
        List<DeviceFirmwareHistoryInfos> firmwareVersionList = new ArrayList<>();
        for (DeviceMessage deviceMessage : deviceMessageList) {
            firmwareVersionList.add(new DeviceFirmwareHistoryInfos(deviceMessage, versionUtils, thesaurus));
        }
        return firmwareVersionList;
    }

    private List<DeviceFirmwareHistoryInfos> sortDescendingByUploadedOnTimestampDeviceFirmwareHistoryInfos(List<DeviceFirmwareHistoryInfos> firmwareVersionList) {
        return firmwareVersionList.stream()
                .sorted(Comparator.comparing(DeviceFirmwareHistoryInfos::getUploadedOn).reversed())
                .collect(Collectors.toList());
    }
}
