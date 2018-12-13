/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.DeviceFirmwareHistory;
import com.energyict.mdc.firmware.DeviceFirmwareVersionHistoryRecord;
import com.energyict.mdc.firmware.FirmwareService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceFirmwareHistoryInfoFactory {

    private final FirmwareService firmwareService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceFirmwareHistoryInfoFactory(Thesaurus thesaurus, FirmwareService firmwareService) {
        this.thesaurus = thesaurus;
        this.firmwareService = firmwareService;
    }

    public DeviceFirmwareHistoryInfos createDeviceFirmwareHistoryInfos(Device device) {
        return new DeviceFirmwareHistoryInfos(firmwareService.getFirmwareHistory(device), thesaurus);
    }

    static class DeviceFirmwareHistoryInfos {
        public int total = 0;
        public List<DeviceFirmwareHistoryInfo> deviceFirmwareHistoryInfos = new ArrayList<>();

        DeviceFirmwareHistoryInfos(DeviceFirmwareHistory history, Thesaurus thesaurus){
            List<DeviceFirmwareVersionHistoryRecord> allHistory = history.history();
            this.total = allHistory.size();
            deviceFirmwareHistoryInfos = allHistory.stream().map(historyRecord -> new DeviceFirmwareHistoryInfo(historyRecord, thesaurus)).collect(Collectors.toList());
        }
    }

    static class DeviceFirmwareHistoryInfo {
        public String firmwareVersion;
        public String firmwareType;
        public Instant activationDate;

        DeviceFirmwareHistoryInfo(DeviceFirmwareVersionHistoryRecord historyRecord, Thesaurus thesaurus){
            this.firmwareVersion = historyRecord.getFirmwareVersion().getFirmwareVersion();
            this.firmwareType = thesaurus.getString(
                historyRecord.getFirmwareVersion().getFirmwareType().getType(),
                historyRecord.getFirmwareVersion().getFirmwareType().getDescription() );
            this.activationDate = historyRecord.getInterval().getStart();
        }
    }

}
