package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.google.common.base.Optional;

import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.messaging.h2")
public class TransientMessageService implements MessageService {

    private final Map<String, TransientQueueTableSpec> queueTableSpecs = new HashMap<>();
    
    @Inject
    public TransientMessageService() {
    	install();
    }

    @Override
    public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer) {
        if (queueTableSpecs.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        if (multiConsumer) {
            TransientQueueTableSpec topic = TransientQueueTableSpec.createTopic(name, payloadType);
            queueTableSpecs.put(name, topic);
            return topic;
        }
        TransientQueueTableSpec queue = TransientQueueTableSpec.createQueue(name, payloadType);
        queueTableSpecs.put(name, queue);
        return queue;
    }

    @Override
    public Optional<QueueTableSpec> getQueueTableSpec(String name) {
        return Optional.<QueueTableSpec>fromNullable(queueTableSpecs.get(name));
    }

    @Override
    public Optional<DestinationSpec> getDestinationSpec(String name) {
        for (TransientQueueTableSpec queueTableSpec : queueTableSpecs.values()) {
            TransientDestinationSpec destination = queueTableSpec.getDestination(name);
            if (destination != null) {
                return Optional.<DestinationSpec>of(destination);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        Optional<DestinationSpec> destinationSpec = getDestinationSpec(destinationSpecName);
        if (!destinationSpec.isPresent()) {
            return Optional.absent();
        }
        for (SubscriberSpec subscriberSpec : destinationSpec.get().getSubscribers()) {
            if (subscriberSpec.getName().equals(name)) {
                return Optional.of(subscriberSpec);
            }
        }
        return Optional.absent();
    }

    private void install() {
    	createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
		createQueueTableSpec("MSG_RAWTOPICTABLE" , "RAW", true);
    }

}
