package com.energyict.mdc.firmware;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface FirmwareCampaign extends HasId, HasName{
    void setName(String name);

    FirmwareCampaignStatus getStatus();

    DeviceType getDeviceType();

    FirmwareType getFirmwareType();

    void setFirmwareType(FirmwareType firmwareType);

    ProtocolSupportedFirmwareOptions getUpgradeOption();

    void setUpgradeOption(ProtocolSupportedFirmwareOptions upgradeOption);

    FirmwareVersion getFirmwareVersion();

    void setFirmwareVersion(FirmwareVersion firmwareVersion);

    Instant getPlannedDate();

    void setPlannedDate(Instant plannedDate);

    Instant getStartedOn();

    void setStartedOn(Instant startedOn);

    Instant getFinishedOn();

    void setFinishedOn(Instant finishedOn);

    List<DeviceInFirmwareCampaign> getDevices();

    Map<String, Object> getProperties();

    FirmwareCampaign addProperty(String key, Object value);

    void clearProperties();

    void save();

    void delete();

    void start();
}
