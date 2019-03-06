/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface TimeOfUseCampaignService {
    String COMPONENT_NAME = "TOU";

    QueryStream<? extends TimeOfUseCampaign> streamAllCampaigns();

    DeviceConfigurationService getDeviceConfigurationService();

    void createToUCampaign(TimeOfUseCampaign timeOfUseCampaign);

    Map<DefaultState, Long> getChildrenStatusFromCampaign(long id);

    TimeOfUseCampaignBuilder newToUbuilder(String name, long deviceType, String deviceGroup,
                                           Instant activationStart, Instant activationEnd, long calendar,
                                           String activationOption, Instant activationDate, String updateType, long validationTimeout);

    Optional<TimeOfUseCampaign> getCampaign(long id);

    Optional<TimeOfUseCampaign> getCampaign(String name);

    Optional<TimeOfUseCampaign> getCampaignOn(ComTaskExecution comTaskExecution);

    QueryStream<? extends TimeOfUseItem> streamDevicesInCampaigns();

    List<DeviceType> getDeviceTypesWithCalendars();

    Pair<Device, ServiceCall> retryDevice(long id);

    Pair<Device, ServiceCall> cancelDevice(Device device);

    Pair<Device, ServiceCall> cancelDevice(long id);

    void cancelCampaign(String campaign);

    Device findDeviceByServiceCall(ServiceCall serviceCall);

    void edit(String name, TimeOfUseCampaign timeOfUseCampaign);

    Optional<ServiceCall> findCampaignServiceCall(String campaignName);

    Optional<TimeOfUseCampaign> findAndLockToUCampaignByIdAndVersion(long id, long version);
}
