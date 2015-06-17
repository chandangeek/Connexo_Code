package com.energyict.mdc.firmware;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FirmwareCampaign extends HasId, HasName{
    void setName(String name);

    FirmwareCampaignStatus getStatus();

    DeviceType getDeviceType();

    FirmwareType getFirmwareType();

    void setFirmwareType(FirmwareType firmwareType);

    FirmwareVersion getFirmwareVersion();

    ProtocolSupportedFirmwareOptions getFirmwareManagementOption();

    void setManagementOption(ProtocolSupportedFirmwareOptions upgradeOption);

    Instant getStartedOn();

    void setStartedOn(Instant startedOn);

    Instant getFinishedOn();

    void setFinishedOn(Instant finishedOn);

    List<DeviceInFirmwareCampaign> getDevices();

    Map<String, Object> getProperties();

    Optional<DeviceMessageSpec> getFirmwareMessageSpec();

    FirmwareCampaign addProperty(String key, String value);

    void clearProperties();

    void save();

    void delete();

    Map<String, Long> getDevicesStatusMap();
}
