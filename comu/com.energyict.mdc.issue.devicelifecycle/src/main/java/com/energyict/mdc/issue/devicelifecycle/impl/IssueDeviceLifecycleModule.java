/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.impl.event.DeviceLifecycleEventHandlerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueDeviceLifecycleModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(QueryService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(EventService.class);
        requireBinding(IssueService.class);
        requireBinding(NlsService.class);
        requireBinding(PropertySpecService.class);

        bind(IssueDeviceLifecycleService.class).to(IssueDeviceLifecycleServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceLifecycleEventHandlerFactory.class).in(Scopes.SINGLETON);
    }

}