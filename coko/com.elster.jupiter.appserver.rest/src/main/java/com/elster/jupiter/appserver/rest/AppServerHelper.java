package com.elster.jupiter.appserver.rest;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;

import javax.inject.Inject;

/**
 * Created by bvn on 8/5/15.
 */
public class AppServerHelper {
    private final AppService appService;

    @Inject
    public AppServerHelper(AppService appService) {
        this.appService = appService;
    }

    public boolean verifyActiveAppServerExists(String destinationName) {
        return appService.findAppServers().stream().
                filter(AppServer::isActive).
                flatMap(server->server.getSubscriberExecutionSpecs().stream()).
                map(execSpec->execSpec.getSubscriberSpec().getDestination()).
                filter(DestinationSpec::isActive).
                filter(spec -> !spec.getSubscribers().isEmpty()).
                anyMatch(spec -> destinationName.equals(spec.getName()));
    }


}
