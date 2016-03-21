package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DeviceFirmwareHistoryInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public DeviceFirmwareHistoryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceFirmwareHistoryInfos createDeviceFirmwareHistoryInfos(Device device) {
        DeviceFirmwareHistoryInfos infos = new DeviceFirmwareHistoryInfos();

        // Hard coded fake data for now
        // To do: real data for this specific device
        // (be sure to order the data DESCENDING, the most recent activation date first)
        DeviceFirmwareHistoryInfo info1 = new DeviceFirmwareHistoryInfo();
        info1.firmwareType = "Meter firmware";
        info1.firmwareVersion = "NTA-Sim_V_2.1.0";
        info1.activationDate = Instant.now();
        infos.deviceFirmwareHistoryInfos.add(info1);

        DeviceFirmwareHistoryInfo info2 = new DeviceFirmwareHistoryInfo();
        info2.firmwareType = "Communication firmware";
        info2.firmwareVersion = "NTA-Sim_V_1.0.0";
        info2.activationDate = Instant.now().minusSeconds(3600);
        infos.deviceFirmwareHistoryInfos.add(info2);

        infos.total = infos.deviceFirmwareHistoryInfos.size();
        return infos;
    }


    static class DeviceFirmwareHistoryInfos {
        public int total = 0;
        public List<DeviceFirmwareHistoryInfo> deviceFirmwareHistoryInfos = new ArrayList<>();
    }

    static class DeviceFirmwareHistoryInfo {
        public String firmwareVersion;
        public String firmwareType;
        public Instant activationDate;
    }
}
