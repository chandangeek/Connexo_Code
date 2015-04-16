package com.energyict.mdc.firmware.rest.impl;


import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
public class FirmwareVersionInfo {
    public Long id;
    public String firmwareVersion;
    public FirmwareTypeInfo firmwareType;
    public FirmwareStatusInfo firmwareStatus;
    public Integer fileSize;
    public String firmwareFile;

    public static FirmwareVersionInfo from(FirmwareVersion firmware, Thesaurus thesaurus) {
        FirmwareVersionInfo firmwareInfo = new FirmwareVersionInfo();
        firmwareInfo.id = firmware.getId();
        firmwareInfo.firmwareVersion = firmware.getFirmwareVersion();
        firmwareInfo.firmwareStatus = new FirmwareStatusInfo(firmware.getFirmwareStatus(), thesaurus);
        firmwareInfo.firmwareType = new FirmwareTypeInfo(firmware.getFirmwareType(), thesaurus);
        return firmwareInfo;
    }

    public static List<FirmwareVersionInfo> from(List<FirmwareVersion> firmwares, Thesaurus thesaurus) {
        return firmwares.stream().map(fw -> FirmwareVersionInfo.from(fw, thesaurus)).collect(Collectors.toList());
    }
}
