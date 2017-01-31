/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.util.streams.Functions.map;
import static com.elster.jupiter.util.streams.Predicates.on;

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

        return appService.findAppServers()
                .stream()
                .filter(AppServer::isActive)
                .map(AppServer::getSubscriberExecutionSpecs)
                .flatMap(List::stream)
                .filter(on(map(SubscriberExecutionSpec::getSubscriberSpec)
                        .andThen(SubscriberSpec::getDestination)
                        .andThen(DestinationSpec::getName))
                        .test(destinationName::equals))
                .anyMatch(SubscriberExecutionSpec::isActive);
    }


}
