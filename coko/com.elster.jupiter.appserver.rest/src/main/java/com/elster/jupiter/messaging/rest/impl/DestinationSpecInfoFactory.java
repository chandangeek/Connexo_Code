/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

/**
 * Created by bvn on 12/23/15.
 */
public class DestinationSpecInfoFactory {
    private final AppService appService;
    private final Thesaurus thesaurus;

    @Inject
    public DestinationSpecInfoFactory(AppService appService, Thesaurus thesaurus) {
        this.appService = appService;
        this.thesaurus = thesaurus;
    }

    public DestinationSpecInfo from(DestinationSpec destinationSpec) {
        DestinationSpecInfo info = new DestinationSpecInfo();
        info.name = destinationSpec.getName();
        info.type = DestinationType.typeOf(destinationSpec);
        info.buffered = destinationSpec.isBuffered();
        info.retryDelayInSeconds = (int) destinationSpec.retryDelay().getSeconds();
        info.numberOfRetries = destinationSpec.numberOfRetries();
        info.active = destinationSpec.isActive();
        info.version = destinationSpec.getVersion();
        return info;
    }

    public DestinationSpecInfo withStats(DestinationSpec destinationSpec) {
        DestinationSpecInfo info = from(destinationSpec);
        info.numberOfMessages = destinationSpec.numberOfMessages();
        info.numberOFErrors = destinationSpec.errorCount();
        return info;
    }

    public DestinationSpecInfo withAppServers(DestinationSpec destinationSpec) {
        DestinationSpecInfo info = withStats(destinationSpec);
        info.subscriberSpecInfos = destinationSpec.getSubscribers()
                .stream()
                .filter(not(SubscriberSpec::isSystemManaged))
                .map(subscriberSpec -> SubscriberSpecInfo.of(subscriberSpec, appService))
                .collect(Collectors.toList());
        return info;
    }

}
