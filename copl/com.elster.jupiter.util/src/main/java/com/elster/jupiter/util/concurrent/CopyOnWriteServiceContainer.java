package com.elster.jupiter.util.concurrent;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@ProviderType
public class CopyOnWriteServiceContainer<S> implements OptionalServiceContainer<S> {

    private final List<S> services = new CopyOnWriteArrayList<>();
    private final Set<Listener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private class Listener implements Registration {

        private final Predicate<? super S> matcher;
        private BlockingQueue<S> found = new ArrayBlockingQueue<S>(1);

        private Listener(Predicate<? super S> predicate) {
            this.matcher = predicate;
        }

        public void notifyAdded(S element) {
            if (matcher.test(element)) {
                found.offer(element);
            }
        }

        public S get() throws InterruptedException {
            return found.take();
        }

        public S get(long timeout, TimeUnit unit) throws InterruptedException {
            return found.poll(timeout, unit);
        }

        @Override
        public void close() {
            listeners.remove(this);
        }
    }

    private interface Registration extends AutoCloseable {
        @Override
        void close();
    }

    private Listener addListener(Predicate<? super S> matcher) {
        Listener listener = new Listener(matcher);
        listeners.add(listener);
        return listener;
    }

    @Override
    public void register(S s) {
        services.add(s);
        listeners.forEach(l -> l.notifyAdded(s));
    }

    @Override
    public void unregister(S s) {
        services.remove(s);
    }

    @Override
    public S get(Predicate<? super S> matcher) throws InterruptedException {
        try (Listener listener = addListener(matcher)) {
            Optional<S> found = poll(matcher);
            if (found.isPresent()) {
                return found.get();
            }
            return listener.get();
        }
    }

    @Override
    public Optional<S> get(Predicate<? super S> matcher, Duration timeout) throws InterruptedException {
        try (Listener listener = addListener(matcher)) {
            Optional<S> found = poll(matcher);
            if (found.isPresent()) {
                return found;
            }
            return Optional.ofNullable(listener.get(timeout.toNanos(), TimeUnit.NANOSECONDS));
        }
    }

    @Override
    public Optional<S> poll(Predicate<? super S> matcher) {
        return services.stream()
                .filter(matcher)
                .findFirst();
    }

    @Override
    public List<S> getServices() {
        return services;
    }
}
