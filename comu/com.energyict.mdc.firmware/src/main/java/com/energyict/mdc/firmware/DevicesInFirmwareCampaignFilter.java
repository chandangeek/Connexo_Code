package com.energyict.mdc.firmware;

import com.elster.jupiter.util.conditions.*;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.impl.DeviceInFirmwareCampaignImpl;
import com.energyict.mdc.firmware.impl.DevicesInFirmwareCampaignStatusImpl;
import com.energyict.mdc.tasks.ComTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Models the filtering that can be applied by client code to count
 * or find {@link DeviceInFirmwareCampaign}s
 * by a number of criteria that can be mixed.
 */
public interface DevicesInFirmwareCampaignFilter {

    /**
     * Sets the single {@link FirmwareCampaign} criterion of the filter
     * @param firmwareCampaignId criterion:  id of the firmware campaign
     * @return the filter
     */
    DevicesInFirmwareCampaignFilter withFirmwareCampaignId(Long firmwareCampaignId);

    /**
     * Sets a List of {@link FirmwareManagementDeviceStatus} as criterion of the filter
     * @param firmwareManagementDeviceStatusKeys: each FirmwareManagementDeviceStatus is uniquely defined by its key()
     * @return the filter
     */
    DevicesInFirmwareCampaignFilter withStatus(List<String> firmwareManagementDeviceStatusKeys);

    /**
     * Returns the filter's condition
     * @return the filter's condition
     */
    Condition getCondition();

}