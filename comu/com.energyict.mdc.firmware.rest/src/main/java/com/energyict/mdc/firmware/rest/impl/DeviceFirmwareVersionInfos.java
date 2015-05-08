package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceFirmwareVersionInfos {
    private Thesaurus thesaurus;
    public List<DeviceFirmwareVersionInfo> firmwares;

    public DeviceFirmwareVersionInfos(Thesaurus thesaurus){
        this.thesaurus = thesaurus;

        this.firmwares = new ArrayList<>(2);
        DeviceFirmwareVersionInfo meterFirmware = new DeviceFirmwareVersionInfo();
        meterFirmware.firmwareType = new FirmwareTypeInfo(FirmwareType.METER, thesaurus);
        this.firmwares.add(meterFirmware);

        DeviceFirmwareVersionInfo communicationFirmware = new DeviceFirmwareVersionInfo();
        communicationFirmware.firmwareType = new FirmwareTypeInfo(FirmwareType.COMMUNICATION, thesaurus);
        this.firmwares.add(communicationFirmware);
    }

    public void addActiveVersion(ActivatedFirmwareVersion activatedFirmwareVersion){
        this.firmwares.stream()
                .filter(firmware -> firmware.firmwareType.id.equals(activatedFirmwareVersion.getFirmwareVersion().getFirmwareType()))
                .forEach(firmware -> {
                    DeviceFirmwareVersionInfo.ActiveVersion activeVersion = new DeviceFirmwareVersionInfo.ActiveVersion();
                    activeVersion.firmwareVersion = activatedFirmwareVersion.getFirmwareVersion().getFirmwareVersion();
                    activeVersion.firmwareVersionStatus = new FirmwareStatusInfo(activatedFirmwareVersion.getFirmwareVersion().getFirmwareStatus(), this.thesaurus);
                    activeVersion.lastCheckedDate = activatedFirmwareVersion.getLastChecked() != null ? activatedFirmwareVersion.getLastChecked().toEpochMilli() : null;
                    firmware.activeVersion = activeVersion;
                });
    }

    public void addUpgradeVersion(String versionName, Map<String, Object> properties, FirmwareVersion version){
        this.firmwares.stream()
                .filter(firmware -> firmware.firmwareType.id.equals(version.getFirmwareType()))
                .forEach(firmware -> firmware.upgradeVersions.put(versionName, properties));
    }
}
