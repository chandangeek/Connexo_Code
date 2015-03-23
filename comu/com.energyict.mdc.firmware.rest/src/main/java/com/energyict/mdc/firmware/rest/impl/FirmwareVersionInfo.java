package com.energyict.mdc.firmware.rest.impl;


import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.stream.Collectors;

public class FirmwareVersionInfo {
    public Long id;
    public String firmwareVersion;
    @XmlJavaTypeAdapter(FirmwareTypeAdapter.class)
    public FirmwareType firmwareType;
    @XmlJavaTypeAdapter(FirmwareStatusAdapter.class)
    public FirmwareStatus firmwareStatus;
    public Long fileSize;



    public static FirmwareVersionInfo from(FirmwareVersion firmware) {
        FirmwareVersionInfo firmwareInfo = new FirmwareVersionInfo();
        firmwareInfo.id = firmware.getId();
        firmwareInfo.firmwareVersion = firmware.getFirmwareVersion();
        firmwareInfo.firmwareStatus = firmware.getFirmwareStatus();
        firmwareInfo.firmwareType = firmware.getFirmwareType();
        return firmwareInfo;
    }

    public static List<FirmwareVersionInfo> from(List<FirmwareVersion> firmwares) {
        return firmwares.stream().map(FirmwareVersionInfo::from).collect(Collectors.toList());
    }
}
