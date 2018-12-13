/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;

import javax.inject.Inject;
import java.util.Optional;

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
        Save.CREATE.save(dataModel, server);

        Optional<DestinationSpec> found = messageService.getDestinationSpec(server.messagingName());
        if (found.isPresent()) { // possibly queue exists from before.
            return server;
        }

        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(server.messagingName(), DEFAULT_RETRY_DELAY_IN_SECONDS);
        destinationSpec.activate();
        destinationSpec.subscribeSystemManaged(server.messagingName());
        Optional<DestinationSpec> allServersTopic = messageService.getDestinationSpec(AppService.ALL_SERVERS);
        if (allServersTopic.isPresent()) {
            allServersTopic.get().subscribeSystemManaged(server.messagingName());
        }
        return server;
    }

}
