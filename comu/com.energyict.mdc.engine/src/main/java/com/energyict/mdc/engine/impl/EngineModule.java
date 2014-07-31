package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactoryImpl;
import com.energyict.mdc.engine.impl.status.StatusServiceImpl;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactoryImpl;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiServiceFactory;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiServiceFactoryImpl;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
* Copyrights EnergyICT
* Date: 22/05/2014
* Time: 11:30
*/
public class EngineModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        requireBinding(TransactionService.class);
        requireBinding(Clock.class);
        requireBinding(HexService.class);
        requireBinding(EngineModelService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(TaskHistoryService.class);
        requireBinding(IssueService.class);
        requireBinding(DeviceDataService.class);
        requireBinding(MdcReadingTypeUtilService.class);
        requireBinding(UserService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(SocketService.class);
        requireBinding(SerialComponentService.class);

        bind(WebSocketQueryApiServiceFactory.class).to(WebSocketQueryApiServiceFactoryImpl.class).in(Scopes.SINGLETON);
        bind(WebSocketEventPublisherFactory.class).to(WebSocketEventPublisherFactoryImpl.class).in(Scopes.SINGLETON);
        bind(EmbeddedWebServerFactory.class).to(DefaultEmbeddedWebServerFactory.class).in(Scopes.SINGLETON);
        bind(ManagementBeanFactory.class).to(ManagementBeanFactoryImpl.class).in(Scopes.SINGLETON);
        bind(StatusService.class).to(StatusServiceImpl.class).in(Scopes.SINGLETON);
        bind(EngineService.class).to(EngineServiceImpl.class).in(Scopes.SINGLETON);
    }

}