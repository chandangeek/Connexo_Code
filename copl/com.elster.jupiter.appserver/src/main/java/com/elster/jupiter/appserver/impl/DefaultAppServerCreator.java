package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.cron.CronExpression;
import com.google.common.base.Optional;

public class DefaultAppServerCreator implements AppServerCreator {

    @Override
    public AppServer createAppServer(final String name, final CronExpression cronExpression) {
        return Bus.getTransactionService().execute(new Transaction<AppServer>() {
            @Override
            public AppServer perform() {
                AppServerImpl server = new AppServerImpl(name, cronExpression);
                Bus.getOrmClient().getAppServerFactory().persist(server);
                QueueTableSpec defaultQueueTableSpec = Bus.getMessageService().getQueueTableSpec("MSG_RAWQUEUETABLE").get();
                DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(server.messagingName(), 60);
                destinationSpec.subscribe(server.messagingName());
                Optional<DestinationSpec> allServersTopic = Bus.getMessageService().getDestinationSpec(AppService.ALL_SERVERS);
                if (allServersTopic.isPresent()) {
                    allServersTopic.get().subscribe(server.messagingName());
                }
                return server;
            }
        });
    }

}
