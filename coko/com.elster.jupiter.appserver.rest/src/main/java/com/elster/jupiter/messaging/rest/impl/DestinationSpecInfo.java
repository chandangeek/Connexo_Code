package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

public class DestinationSpecInfo {

    public String name;
    public DestinationType type;
    public boolean active;
    public boolean buffered;
    public long retryDelayInSeconds;
    public int numberOfRetries;
    public long version;
    public Long numberOfMessages;
    public Long numberOFErrors;
    public List<SubscriberSpecInfo> subscriberSpecInfos;

    public static DestinationSpecInfo from(DestinationSpec destinationSpec) {
        DestinationSpecInfo info = new DestinationSpecInfo();
        info.name = destinationSpec.getName();
        info.type = DestinationType.typeOf(destinationSpec);
        info.buffered = destinationSpec.isBuffered();
        info.retryDelayInSeconds = destinationSpec.retryDelay().getSeconds();
        info.numberOfRetries = destinationSpec.numberOfRetries();
        info.active = destinationSpec.isActive();
        info.version = destinationSpec.getVersion();
        return info;
    }

    public DestinationSpecInfo withStats(DestinationSpec destinationSpec) {
        numberOfMessages = destinationSpec.numberOfMessages();
        numberOFErrors = destinationSpec.errorCount();
        return this;
    }

    public DestinationSpecInfo withAppServers(DestinationSpec destinationSpec, AppService appService, Thesaurus thesaurus) {
        subscriberSpecInfos = destinationSpec.getSubscribers()
                .stream()
                .filter(not(SubscriberSpec::isSystemManaged))
                .map(subscriberSpec -> SubscriberSpecInfo.of(subscriberSpec, appService, thesaurus))
                .collect(Collectors.toList());
        return this;
    }
}
