/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Simple module which defines the required bindings
 */
public class FirmwareModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        requireBinding(QueryService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(DeviceMessageSpecificationService.class);
        requireBinding(DeviceMessageService.class);
        requireBinding(MessageService.class);
        requireBinding(TaskService.class);
        requireBinding(com.elster.jupiter.tasks.TaskService.class);

        bind(FirmwareService.class).to(FirmwareServiceImpl.class).in(Scopes.SINGLETON);
        bind(FirmwareCampaignService.class).to(FirmwareCampaignServiceImpl.class).in(Scopes.SINGLETON);
    }
}
