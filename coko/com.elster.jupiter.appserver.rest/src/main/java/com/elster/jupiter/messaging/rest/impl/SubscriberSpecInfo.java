package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

public class SubscriberSpecInfo {

    public String destination;
    public String subscriber;
    public String displayName;
    public List<SubscriberExecutionSpecInfo> appServers;

    public SubscriberSpecInfo() {
    }

    public SubscriberSpecInfo(String destination, String subscriber, Thesaurus thesaurus) {
        this.destination = destination;
        this.subscriber = subscriber;
        this.displayName = thesaurus.getStringBeyondComponent(subscriber, subscriber);
    }

    public static SubscriberSpecInfo of(SubscriberSpec subscriberSpec, AppService appService, Thesaurus thesaurus) {
        SubscriberSpecInfo info = new SubscriberSpecInfo(subscriberSpec.getDestination().getName(), subscriberSpec.getName(), thesaurus);
        info.appServers = appService.getSubscriberExecutionSpecsFor(subscriberSpec)
                .stream()
                .map(SubscriberExecutionSpecInfo::of)
                .collect(Collectors.toList());
        return info;
    }

}