/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface FirmwareCampaignService {
    public static String COMPONENT_NAME = "FWC";

    Optional<FirmwareCampaign> getFirmwareCampaignById(long id);

    Optional<FirmwareCampaign> getCampaignbyName(String name);

    Optional<FirmwareCampaign> findAndLockFirmwareCampaignByIdAndVersion(long id, long version);

//    Finder<campaign> getFirmwareCampaigns();
//
//    List<campaign> findFirmwareCampaigns(DeviceType deviceType);
//
//    Finder<DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(campaign firmwareCampaign);

    DevicesInFirmwareCampaignFilter filterForDevicesInFirmwareCampaign();

    Finder<DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(DevicesInFirmwareCampaignFilter filter);

    void createItemsOnCampaign(ServiceCall serviceCall);

    void cancelFirmwareUpload(ServiceCall serviceCall);

    FirmwareCampaignBuilder newFirmwareCampaignBuilder(String name);

    void editCampaignItems(FirmwareCampaign source);

    Optional<FirmwareCampaign> getCampaignOn(ComTaskExecution comTaskExecution);

    Optional<DeviceInFirmwareCampaign> findActiveFirmwareItemByDevice(Device device);

    QueryStream<? extends DeviceInFirmwareCampaign> streamDevicesInCampaigns();

    boolean isWithVerification(FirmwareCampaign firmwareCampaign);

    boolean retryFirmwareUploadForDevice(DeviceInFirmwareCampaign deviceInFirmwareCampaign);

    QueryStream<? extends FirmwareCampaign> streamAllCampaigns();

    List<FirmwareCampaign> findFirmwareCampaigns(DeviceType deviceType);

    Optional<DeviceMessageSpec> getFirmwareMessageSpec(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOptions,
                                                       FirmwareVersion firmwareVersion);

    FirmwareVersion getFirmwareVersion(Map<String, Object> properties);
}
