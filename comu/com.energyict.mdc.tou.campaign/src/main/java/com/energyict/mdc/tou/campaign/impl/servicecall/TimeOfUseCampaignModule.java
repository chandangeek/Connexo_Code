/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class TimeOfUseCampaignModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(ThreadPrincipalService.class);
        requireBinding(TransactionService.class);
        requireBinding(NlsService.class);
        requireBinding(UpgradeService.class);
        requireBinding(UserService.class);
        requireBinding(BatchService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(ServiceCallService.class);
        requireBinding(CustomPropertySetService.class);
//        requireBinding(MeteringGroupsService.class);
        requireBinding(Clock.class);
        requireBinding(DeviceService.class);
        requireBinding(CalendarService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(DeviceMessageSpecificationService.class);

        bind(TimeOfUseCampaignService.class).to(TimeOfUseCampaignServiceImpl.class).in(Scopes.SINGLETON);
    }
}