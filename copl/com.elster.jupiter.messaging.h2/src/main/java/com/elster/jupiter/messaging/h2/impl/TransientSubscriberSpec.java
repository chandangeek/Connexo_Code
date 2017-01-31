/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class TransientSubscriberSpec implements SubscriberSpec {

    private final TransientDestinationSpec destinationSpec;
    private final String name;
    private final String displayName;
    private final BlockingQueue<TransientMessage> messages = new LinkedBlockingQueue<>();
    private Thread toCancel;
    private final Object lock = new Object();

    public TransientSubscriberSpec(TransientDestinationSpec destinationSpec, String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        this.destinationSpec = destinationSpec;
    }

    @Override
    public DestinationSpec getDestination() {
        return destinationSpec;
    }

    @Override
    public Message receive() {
        try {
            setToCancel(Thread.currentThread());
            return messages.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            setToCancel(null);
        }
    }

    @Override
    public boolean isSystemManaged() {
        return false;
    }

    private void setToCancel(Thread thread) {
        synchronized (lock) {
            toCancel = thread;
        }
    }

    @Override
    public void cancel() {
        synchronized (lock) {
            if (toCancel != null) {
                toCancel.interrupt();
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void addMessage(TransientMessage transientMessage) {
        messages.add(transientMessage);
    }

    public long messageCount() {
        return messages.size();
    }

    void removeMessagesWithCorrelationId(String correlationId) {
        messages.removeIf(message -> Objects.equals(message.getCorrelationId(), correlationId));
    }
}
