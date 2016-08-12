package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.DequeueOptions;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Optional;

import static com.elster.jupiter.util.streams.Currying.perform;

class DefaultAppServerCreator implements AppServerCreator {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    DefaultAppServerCreator(DataModel dataModel, MessageService messageService) {
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
        this.createSubscriberSpec(destinationSpec, server);
        Optional<DestinationSpec> allServersTopic = messageService.getDestinationSpec(AppService.ALL_SERVERS);
        allServersTopic.ifPresent(perform(this::createSubscriberSpec).with(server));
        return server;
    }

    private SubscriberSpec createSubscriberSpec(DestinationSpec destinationSpec, AppServerImpl server) {
        return destinationSpec
                    .subscribe(server.messagingName())
                    .systemManaged()
                    .with(DequeueOptions
                            .wait(Duration.ofSeconds(60))
                            .retryAfter(Duration.ofSeconds(0)))
                    .create();
    }

}
