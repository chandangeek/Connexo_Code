/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.QueryStream;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface FirmwareCampaignService {

    Optional<FirmwareCampaign> getFirmwareCampaignById(long id);

    Optional<FirmwareCampaign> getCampaignByName(String name);

    Optional<FirmwareCampaign> findAndLockFirmwareCampaignByIdAndVersion(long id, long version);

    DevicesInFirmwareCampaignFilter filterForDevicesInFirmwareCampaign();

    Finder<? extends DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(DevicesInFirmwareCampaignFilter filter);

    FirmwareCampaignBuilder newFirmwareCampaign(String name);

    Optional<FirmwareCampaign> getCampaignOn(ComTaskExecution comTaskExecution);

    Optional<DeviceInFirmwareCampaign> findActiveFirmwareItemByDevice(Device device);

    QueryStream<? extends DeviceInFirmwareCampaign> streamDevicesInCampaigns();

    QueryStream<? extends FirmwareCampaign> streamAllCampaigns();

    List<FirmwareCampaign> findFirmwareCampaigns(DeviceType deviceType);

    List<DeviceInFirmwareCampaign> findFirmwareCampaignItems(Device device);

    FirmwareVersion getFirmwareVersion(Map<String, Object> properties);

}
