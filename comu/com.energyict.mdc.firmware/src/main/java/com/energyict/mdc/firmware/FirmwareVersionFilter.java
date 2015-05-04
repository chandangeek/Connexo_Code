package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirmwareVersionFilter {

    private DeviceType deviceType;
    private List<FirmwareType> firmwareTypes = new ArrayList<>();
    private List<FirmwareStatus> firmwareStatuses = new ArrayList<>();
    private List<String> firmwareVersions = new ArrayList<>();

    public FirmwareVersionFilter (DeviceType deviceType) {
        this.deviceType = deviceType;
    }


    public void setFirmwareTypes(List<FirmwareType> firmwareTypes) {
        this.firmwareTypes.addAll(firmwareTypes);
    }

    public void setFirmwareStatuses(List<FirmwareStatus> firmwareStatuses) {
        this.firmwareStatuses.addAll(firmwareStatuses);
    }

    public void setFirmwareVersions(List<String> firmwareVersions) {
        this.firmwareVersions.addAll(firmwareVersions);
    }

    public List<String> getFirmwareVersions() {
        return firmwareVersions;
    }

    public List<FirmwareType> getFirmwareTypes() {
        return Collections.unmodifiableList(this.firmwareTypes);
    }

    public List<FirmwareStatus> getFirmwareStatuses() {
        return Collections.unmodifiableList(this.firmwareStatuses);
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }
}
