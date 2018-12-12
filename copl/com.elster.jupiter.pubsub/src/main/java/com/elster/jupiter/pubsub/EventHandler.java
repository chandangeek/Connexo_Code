/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pubsub;

import org.osgi.framework.BundleContext;

/**
 * This class is a template method implementation of a Subscriber.
 * It filters for events of a certain type, and then passes only these on to the onEvent() method, to be implemented by subclasses.
 * Aside from that it offers a register() method that will correctly register an instance as a Subscriber.
 *
 * @param <T> the type of the events this Subscriber is interested in.
 */
public abstract class EventHandler<T> implements Subscriber {

    private final Class<T> type;

    protected EventHandler(Class<T> eventType) {
        this.type = eventType;
    }

    /**
     * Dynamically registers this instance as a Subscriber service with the osgi framework through the given BundleContext.
     * @param context
     */
    public final void register(BundleContext context) {
        context.registerService(Subscriber.class, this,null);
    }

    @Override
    public final void handle(Object notification, Object... notificationDetails) {
        if (type.isInstance(notification)) {
            @SuppressWarnings("unchecked") // safe cast since we just checked through introspection
            T typedEvent = (T) notification;
            onEvent(typedEvent, notificationDetails);
        }
    }
    
    @Override
    public final Class<?>[] getClasses(){
    	return new Class<?>[] { type };    
    }

    /**
     * To be implemented by subclasses to perform the effective work that needs to be done on the occurrence of the given event.
     *
     * @param event
     * @param eventDetails
     */
    protected abstract void onEvent(T event, Object... eventDetails);
}
