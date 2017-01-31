/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.messaging.h2", service = {MessageService.class}, property = {"name=MSG"})
public class TransientMessageService implements MessageService {

    private final Map<String, TransientQueueTableSpec> queueTableSpecs = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public TransientMessageService() {
    }

    @Inject
    public TransientMessageService(NlsService nlsService) {
        setNlsService(nlsService);
        activate();
    }

    @Activate
    public void activate() {
        createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
        createQueueTableSpec("MSG_RAWTOPICTABLE", "RAW", true);
    }

    @Override
    public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer) {
        if (queueTableSpecs.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        if (multiConsumer) {
            TransientQueueTableSpec topic = TransientQueueTableSpec.createTopic(thesaurus, name, payloadType);
            queueTableSpecs.put(name, topic);
            return topic;
        }
        TransientQueueTableSpec queue = TransientQueueTableSpec.createQueue(thesaurus, name, payloadType);
        queueTableSpecs.put(name, queue);
        return queue;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MessageService.COMPONENTNAME, Layer.SERVICE);
    }

    @Override
    public Optional<QueueTableSpec> getQueueTableSpec(String name) {
        return Optional.ofNullable(queueTableSpecs.get(name));
    }

    @Override
    public Optional<DestinationSpec> getDestinationSpec(String name) {
        return queueTableSpecs.values().stream()
                .map(spec -> (DestinationSpec) spec.getDestination(name))
                .filter(Objects::nonNull)
                .findAny();
    }

    @Override
    public Optional<DestinationSpec> lockDestinationSpec(String name, long version) {
        return getDestinationSpec(name);
    }

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        Optional<DestinationSpec> destinationSpec = getDestinationSpec(destinationSpecName);
        if (!destinationSpec.isPresent()) {
            return Optional.empty();
        }
        for (SubscriberSpec subscriberSpec : destinationSpec.get().getSubscribers()) {
            if (subscriberSpec.getName().equals(name)) {
                return Optional.of(subscriberSpec);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SubscriberSpec> getSubscribers() {
        return queueTableSpecs.values().stream()
                .flatMap(q -> q.getDestinations().stream())
                .flatMap(d -> d.getSubscribers().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriberSpec> getNonSystemManagedSubscribers() {
        return getSubscribers();
    }

    @Override
    public List<DestinationSpec> findDestinationSpecs() {
        return queueTableSpecs.values().stream()
                .map(TransientQueueTableSpec::getDestinations)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
