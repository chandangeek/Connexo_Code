/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.SubscriberSpec;

import java.util.List;
import java.util.stream.Collectors;

public class SubscriberSpecInfo {

    public String destination;
    public String subscriber;
    public String displayName;
    public List<SubscriberExecutionSpecInfo> appServers;

    public SubscriberSpecInfo() {
    }

    public SubscriberSpecInfo(String destination, String subscriber, String displayName) {
        this.destination = destination;
        this.subscriber = subscriber;
        this.displayName = displayName;
    }

    public static SubscriberSpecInfo of(SubscriberSpec subscriberSpec, AppService appService) {
        SubscriberSpecInfo info = new SubscriberSpecInfo(subscriberSpec.getDestination().getName(), subscriberSpec.getName(), subscriberSpec.getDisplayName());
        info.appServers = appService.getSubscriberExecutionSpecsFor(subscriberSpec)
                .stream()
                .map(SubscriberExecutionSpecInfo::of)
                .collect(Collectors.toList());
        return info;
    }

}