/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import javax.inject.Inject;
import java.util.List;
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

    public DestinationSpecInfo from(DestinationSpec destinationSpec, List<RecurrentTask> allTasks, List<String> allServiceCallTypes) {
        DestinationSpecInfo info = new DestinationSpecInfo();
        info.name = destinationSpec.getName();
        info.type = DestinationType.typeOf(destinationSpec);
        info.buffered = destinationSpec.isBuffered();
        info.retryDelayInSeconds = (int) destinationSpec.retryDelay().getSeconds();
        info.numberOfRetries = destinationSpec.numberOfRetries();
        info.active = destinationSpec.isActive();
        info.version = destinationSpec.getVersion();
        info.isDefault = destinationSpec.isDefault();
        info.queueTypeName = destinationSpec.getQueueTypeName();
        info.tasks = getTasksFrom(destinationSpec.getName(), allTasks);
        info.serviceCallTypes = allServiceCallTypes.stream().filter(name -> destinationSpec.getName().equals(name)).collect(Collectors.toList());

        return info;
    }

    private List<TaskMinInfo> getTasksFrom(String queueName, List<RecurrentTask> allTasks) {
        return allTasks.stream().filter(task -> queueName.equals(task.getDestination().getName()))
                .map(rt -> TaskMinInfo.from(rt, thesaurus)).collect(Collectors.toList());
    }

    public DestinationSpecInfo withStats(DestinationSpec destinationSpec, List<RecurrentTask> allTasks, List<String> allServiceCallTypes) {
        DestinationSpecInfo info = from(destinationSpec, allTasks, allServiceCallTypes);
        info.numberOfMessages = destinationSpec.numberOfMessages();
        info.numberOFErrors = destinationSpec.errorCount();
        return info;
    }

    public DestinationSpecInfo withAppServers(DestinationSpec destinationSpec, List<RecurrentTask> allTasks, List<String> allServiceCallTypes) {
        DestinationSpecInfo info = withStats(destinationSpec, allTasks, allServiceCallTypes);
        info.subscriberSpecInfos = destinationSpec.getSubscribers()
                .stream()
                .filter(not(SubscriberSpec::isSystemManaged))
                .map(subscriberSpec -> SubscriberSpecInfo.of(subscriberSpec, appService))
                .collect(Collectors.toList());
        return info;
    }

}
