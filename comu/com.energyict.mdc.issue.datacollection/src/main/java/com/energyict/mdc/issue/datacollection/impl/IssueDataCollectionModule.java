package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventHandlerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueDataCollectionModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(QueryService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(EventService.class);
        requireBinding(IssueService.class);
        requireBinding(NlsService.class);
        requireBinding(DeviceService.class);
        requireBinding(CommunicationTaskService.class);
        requireBinding(ConnectionTaskService.class);
        requireBinding(PropertySpecService.class);

        bind(IssueDataCollectionService.class).to(IssueDataCollectionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataCollectionEventHandlerFactory.class).in(Scopes.SINGLETON);
    }
}
