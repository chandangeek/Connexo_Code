/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
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

    Map<DefaultState, Long> getChildrenStatusFromCampaign(long id);

    TimeOfUseCampaignBuilder newTouCampaignBuilder(String name, long typeId, long calendarId);

    Optional<TimeOfUseCampaign> getCampaign(long id);

    Optional<TimeOfUseCampaign> getCampaign(String name);

    Optional<TimeOfUseCampaign> getCampaignOn(ComTaskExecution comTaskExecution);

    QueryStream<? extends TimeOfUseItem> streamDevicesInCampaigns();

    List<DeviceType> getDeviceTypesWithCalendars();

    ServiceCall retryDevice(Device device);

    ServiceCall cancelDevice(Device device);

    void cancelCampaign(long id);

    void deleteCampaign(long id);

    Optional<ServiceCall> findActiveServiceCallByDevice(Device device);

    void edit(long id, String name, Instant start, Instant end);

    Optional<TimeOfUseCampaign> findAndLockToUCampaignByIdAndVersion(long id, long version);

    Optional<ServiceCall> findAndLockToUItemByIdAndVersion(long id, long version);
}
