package com.elster.jupiter.pubsub;

import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Hashtable;

public abstract class EventHandler<T> implements Subscriber {

    private final Class<T> type;

    public EventHandler(Class<T> eventType) {
        this.type = eventType;
    }

    public final void register(BundleContext context) {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put(Subscriber.TOPIC, type);
        context.registerService(Subscriber.class, this, dictionary);
    }

    @Override
    public final void handle(Object event, Object... eventDetails) {
        if (type.isInstance(event)) {
            @SuppressWarnings("unchecked") // safe cast since we just checked through introspection
            T typedEvent = (T) event;
            onEvent(typedEvent, eventDetails);
        }
    }

    protected abstract void onEvent(T event, Object... eventDetails);
}
