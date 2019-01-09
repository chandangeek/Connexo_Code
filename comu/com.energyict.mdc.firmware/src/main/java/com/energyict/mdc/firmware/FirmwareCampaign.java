/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.upl.properties.DeviceGroup;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface FirmwareCampaign extends HasId, HasName{
    void setName(String name);

    FirmwareCampaignStatus getStatus();

    DeviceType getDeviceType();

    EndDeviceGroup getDeviceGroup();  //lori

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

    long getVersion();

    ComWindow getComWindow();

    void setComWindow(ComWindow window);

    void decreaseCount();

    Optional<TimeDuration> getValidationTimeout();

    void setValidationTimeout(TimeDuration validationTimeout);
}
