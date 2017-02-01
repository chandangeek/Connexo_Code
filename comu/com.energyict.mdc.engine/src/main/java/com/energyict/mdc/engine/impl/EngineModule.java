/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactoryImpl;
import com.energyict.mdc.engine.impl.status.StatusServiceImpl;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class EngineModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        requireBinding(TransactionService.class);
        requireBinding(Clock.class);
        requireBinding(HexService.class);
        requireBinding(EngineConfigurationService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(IssueService.class);
        requireBinding(DeviceService.class);
        requireBinding(MdcReadingTypeUtilService.class);
        requireBinding(UserService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(SocketService.class);
        requireBinding(SerialComponentService.class);

        bind(ManagementBeanFactory.class).to(ManagementBeanFactoryImpl.class).in(Scopes.SINGLETON);
        bind(StatusService.class).to(StatusServiceImpl.class).in(Scopes.SINGLETON);
        bind(EngineService.class).to(EngineServiceImpl.class).in(Scopes.SINGLETON);
    }

}