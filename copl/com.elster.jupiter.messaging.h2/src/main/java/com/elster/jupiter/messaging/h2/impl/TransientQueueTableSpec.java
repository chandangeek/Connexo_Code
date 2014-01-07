package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class TransientQueueTableSpec implements QueueTableSpec {

    private boolean active;
    private String payloadType;
    private State state;
    private String name;
    private List<TransientDestinationSpec> destinations = new CopyOnWriteArrayList<>();

    public static TransientQueueTableSpec createTopic(String name, String payloadType) {
        return new TransientQueueTableSpec(name, payloadType, States.TOPIC);
    }

    public static TransientQueueTableSpec createQueue(String name, String payloadType) {
        return new TransientQueueTableSpec(name, payloadType, States.QUEUE);
    }

    private TransientQueueTableSpec(String name, String payloadType, State state) {
        this.name = name;
        this.payloadType = payloadType;
        this.state = state;
    }

    public TransientDestinationSpec getDestination(String name) {
        for (TransientDestinationSpec destination : destinations) {
            if (destination.getName().equals(name)) {
                return destination;
            }
        }
        return null;
    }

    private interface State {

        boolean isMultiConsumer();

        boolean isJms();
    }

    private enum States implements State {
        TOPIC {
            @Override
            public boolean isJms() {
                return false;
            }

            @Override
            public boolean isMultiConsumer() {
                return true;
            }
        }, QUEUE {
            @Override
            public boolean isJms() {
                return true;
            }

            @Override
            public boolean isMultiConsumer() {
                return false;
            }
        }
    }

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public void deactivate() {
        active = false;
    }

    @Override
    public String getPayloadType() {
        return payloadType;
    }

    @Override
    public boolean isMultiConsumer() {
        return state.isMultiConsumer();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public DestinationSpec createDestinationSpec(String name, int retryDelay) {
        TransientDestinationSpec destinationSpec = new TransientDestinationSpec(this, name);
        destinations.add(destinationSpec);
        return destinationSpec;
    }

    @Override
    public boolean isJms() {
        return state.isJms();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void save() {
    }
}
