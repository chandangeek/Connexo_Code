/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueStatus;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.tasks.RecurrentTask;

import aQute.bnd.osgi.resource.FilterParser;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public DestinationSpecInfo from(DestinationSpec destinationSpec, List<RecurrentTask> allTasks, List<ServiceCallType> serviceCallTypes) {
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
        info.serviceCallTypes = serviceCallTypes.stream().map(sct -> sct.getName()).collect(Collectors.toList());

        return info;
    }

    private List<TaskMinInfo> getTasksFrom(String queueName, List<RecurrentTask> allTasks) {
        return allTasks.stream().filter(task -> queueName.equals(task.getDestination().getName()))
                .map(rt -> TaskMinInfo.from(rt, thesaurus)).collect(Collectors.toList());
    }

    public DestinationSpecInfo withStats(DestinationSpec destinationSpec, List<RecurrentTask> allTasks, List<ServiceCallType> allServiceCallTypes, Map<String, QueueStatus> queuesStatuses) {
        DestinationSpecInfo info = from(destinationSpec, allTasks, allServiceCallTypes);
        QueueStatus queueStatus = Optional.ofNullable(queuesStatuses).map(qs -> qs.get(destinationSpec.getName())).orElse(null);
        info.numberOfMessages = Optional.ofNullable(queueStatus).map(QueueStatus::getMessagesCount).orElseGet(destinationSpec::numberOfMessages);
        info.numberOFErrors = Optional.ofNullable(queueStatus).map(QueueStatus::getErrorsCount).orElseGet(destinationSpec::errorCount);
        return info;
    }

    public DestinationSpecInfo withAppServers(DestinationSpec destinationSpec, List<RecurrentTask> allTasks, List<ServiceCallType> allServiceCallTypes, Map<String, QueueStatus> queuesStatuses) {
        DestinationSpecInfo info = withStats(destinationSpec, allTasks, allServiceCallTypes, queuesStatuses);
        info.subscriberSpecInfos = destinationSpec.getSubscribers()
                .stream()
                .filter(not(SubscriberSpec::isSystemManaged))
                .map(subscriberSpec -> SubscriberSpecInfo.of(subscriberSpec, appService))
                .collect(Collectors.toList());
        return info;
    }

}
