package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class TransientSubscriberSpec implements SubscriberSpec {

    private final TransientDestinationSpec destinationSpec;
    private final String name;
    private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private Thread toCancel;
    private final Object lock = new Object();

    public TransientSubscriberSpec(TransientDestinationSpec destinationSpec, String name) {
        this.name = name;
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

    public void addMessage(TransientMessage transientMessage) {
        messages.add(transientMessage);
    }
}
