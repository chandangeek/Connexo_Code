/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;

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

    String getCalendarUploadConnectionStrategyTranslation(ConnectionStrategy connectionStrategy);

    String getValidationConnectionStrategyTranslation(ConnectionStrategy connectionStrategy);
}
