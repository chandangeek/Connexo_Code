package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.google.common.base.Optional;

import javax.inject.Inject;

public class DefaultAppServerCreator implements AppServerCreator {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public DefaultAppServerCreator(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public AppServer createAppServer(final String name, final CronExpression cronExpression) {
        AppServerImpl server = AppServerImpl.from(dataModel, name, cronExpression);
        dataModel.mapper(AppServer.class).persist(server);
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(server.messagingName(), DEFAULT_RETRY_DELAY_IN_SECONDS);
        destinationSpec.activate();
        destinationSpec.subscribe(server.messagingName());
        Optional<DestinationSpec> allServersTopic = messageService.getDestinationSpec(AppService.ALL_SERVERS);
        if (allServersTopic.isPresent()) {
            allServersTopic.get().subscribe(server.messagingName());
        }
        return server;
    }

}
