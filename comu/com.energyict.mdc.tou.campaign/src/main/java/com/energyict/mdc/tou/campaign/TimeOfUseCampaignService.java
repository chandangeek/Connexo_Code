/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface TimeOfUseCampaignService {
    String COMPONENT_NAME = "TOU";

    QueryStream<? extends TimeOfUseCampaign> streamAllCampaigns();

    TimeOfUseCampaignBuilder newTouCampaignBuilder(String name, DeviceType deviceType, Calendar calendar);

    Optional<TimeOfUseCampaign> getCampaign(long id);

    Optional<TimeOfUseCampaign> getCampaign(String name);

    Optional<TimeOfUseCampaign> getCampaignOn(ComTaskExecution comTaskExecution);

    QueryStream<? extends TimeOfUseCampaignItem> streamDevicesInCampaigns();

    List<DeviceType> getDeviceTypesWithCalendars();

    Optional<TimeOfUseCampaignItem> findActiveTimeOfUseItemByDevice(Device device);

    Optional<TimeOfUseCampaign> findAndLockToUCampaignByIdAndVersion(long id, long version);

    Optional<ServiceCall> findAndLockToUItemByIdAndVersion(long id, long version);

    ComTask getComTaskById(long id);

}
