/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceFirmwareLifecycleHistoryInfoFactory {

    private final FirmwareService firmwareService;
    private volatile Thesaurus thesaurus;

    @Inject
    public DeviceFirmwareLifecycleHistoryInfoFactory(FirmwareService firmwareService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    public List<DeviceFirmwareLifecycleHistoryInfo> getDeviceFirmwareHistoryInfosListFromDevice(Device device) {
        FirmwareManagementDeviceUtils versionUtils = firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        List<DeviceFirmwareLifecycleHistoryInfo> firmwareVersionList = getDeviceFirmwareHistoryInfos(versionUtils);
        List<DeviceFirmwareLifecycleHistoryInfo> deviceFirmwareLifecycleHistoryInfos = sortDescendingByUploadedOnTimestampDeviceFirmwareHistoryInfos(firmwareVersionList);
        deviceFirmwareLifecycleHistoryInfos.stream().forEach(deviceFirmwareLifecycleHistoryInfo -> {
            if(deviceFirmwareLifecycleHistoryInfo.getResult().equals("Pending")){
                deviceFirmwareLifecycleHistoryInfo.setUploadedOn(null);
                deviceFirmwareLifecycleHistoryInfo.setActivationDate(null);
            }
        });
        return deviceFirmwareLifecycleHistoryInfos;
    }

    private List<DeviceFirmwareLifecycleHistoryInfo> getDeviceFirmwareHistoryInfos(FirmwareManagementDeviceUtils versionUtils) {
        List<DeviceFirmwareLifecycleHistoryInfo> firmwareVersionList = new ArrayList<>();
        List<DeviceMessage> deviceMessageList = versionUtils.getFirmwareMessages();
        for (DeviceMessage deviceMessage : deviceMessageList) {
            firmwareVersionList.add(new DeviceFirmwareLifecycleHistoryInfo(deviceMessage, versionUtils, thesaurus));
        }
        return firmwareVersionList;
    }

    private List<DeviceFirmwareLifecycleHistoryInfo> sortDescendingByUploadedOnTimestampDeviceFirmwareHistoryInfos(List<DeviceFirmwareLifecycleHistoryInfo> firmwareVersionList) {
        return firmwareVersionList.stream()
                .sorted(Comparator.comparing(DeviceFirmwareLifecycleHistoryInfo::getUploadedOn).reversed())
                .collect(Collectors.toList());
    }
}
