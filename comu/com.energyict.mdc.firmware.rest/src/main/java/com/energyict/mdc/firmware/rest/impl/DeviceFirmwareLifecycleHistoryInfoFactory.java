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

import com.google.common.base.Function;

import javax.inject.Inject;
import java.time.Instant;
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
        return sortDeviceFirmwareHistoryInfosDescendingByUploadTimestamp(firmwareVersionList);
    }

    private List<DeviceFirmwareLifecycleHistoryInfo> getDeviceFirmwareHistoryInfos(FirmwareManagementDeviceUtils versionUtils) {
        List<DeviceFirmwareLifecycleHistoryInfo> firmwareVersionList = new ArrayList<>();
        List<DeviceMessage> deviceMessageList = versionUtils.getFirmwareMessages();
        for (DeviceMessage deviceMessage : deviceMessageList) {
            firmwareVersionList.add(new DeviceFirmwareLifecycleHistoryInfo(deviceMessage, versionUtils, thesaurus));
        }
        return firmwareVersionList;
    }

    private List<DeviceFirmwareLifecycleHistoryInfo> sortDeviceFirmwareHistoryInfosDescendingByUploadTimestamp(List<DeviceFirmwareLifecycleHistoryInfo> firmwareVersionList) {
        Comparator<DeviceFirmwareLifecycleHistoryInfo> comparator = Comparator.comparing(deviceFirmwareLifecycleHistoryInfo ->
                (deviceFirmwareLifecycleHistoryInfo.getUploadDate() == null) ? Instant.MAX : deviceFirmwareLifecycleHistoryInfo.getUploadDate());
        comparator = comparator.thenComparing(DeviceFirmwareLifecycleHistoryInfo::getPlannedDate).reversed();
        return firmwareVersionList.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
