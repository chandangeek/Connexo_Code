package com.elster.jupiter.util.concurrent;

import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.GuardedBy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapServiceContainer<K, S> implements OptionalServiceContainer<S> {

    private final Object lock = new Object();
    @GuardedBy("lock")
    private final Map<K, S> services = new HashMap<>();
    private final Set<Listener<S>> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Function<S, K> keyExtractor;

    public MapServiceContainer(Function<S, K> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    private interface Listener<E> {
        void notifyAdded(E element);
    }

    private class BlockingListener<E> implements Registration, Listener<E> {

        private final Predicate<? super E> matcher;
        private BlockingQueue<E> found = new LinkedBlockingQueue<>();

        private BlockingListener(Predicate<? super E> predicate) {
            this.matcher = predicate;
        }

        @Override
        public void notifyAdded(E element) {
            if (matcher.test(element)) {
                found.offer(element);
            }
        }

        public E get() throws InterruptedException {
            return found.take();
        }

        public E get(long timeout, TimeUnit unit) throws InterruptedException {
            return found.poll(timeout, unit);
        }

        @Override
        public void close() {
            listeners.remove(this);
        }
    }

    private class ReactingListener<E> implements Listener<E> {

        private final Predicate<? super E> predicate;
        private final Consumer<E> consumer;

        private ReactingListener(Predicate<? super E> predicate, Consumer<E> consumer) {
            this.predicate = predicate;
            this.consumer = consumer;
        }

        @Override
        public void notifyAdded(E element) {
            if (predicate.test(element)) {
                consumer.accept(element);
            }
        }
    }

    private interface Registration extends AutoCloseable {
        @Override
        void close();
    }

    private BlockingListener<S> addListener(Predicate<? super S> matcher) {
        BlockingListener<S> listener = new BlockingListener<>(matcher);
        listeners.add(listener);
        return listener;
    }

    @Override
    public void register(S s) {
        synchronized (lock) {
            services.put(keyExtractor.apply(s), s);
        }
        listeners.forEach(l -> l.notifyAdded(s));
    }

    @Override
    public void unregister(S s) {
        synchronized (lock) {
            services.remove(keyExtractor.apply(s));
        }
    }

    @Override
    public S get(Predicate<? super S> matcher) throws InterruptedException {
        try (BlockingListener<S> listener = addListener(matcher)) {
            Optional<S> found = poll(matcher);
            if (found.isPresent()) {
                return found.get();
            }
            return listener.get();
        }
    }

    @Override
    public Optional<S> get(Predicate<? super S> matcher, Duration timeout) throws InterruptedException {
        try (BlockingListener<S> listener = addListener(matcher)) {
            Optional<S> found = poll(matcher);
            if (found.isPresent()) {
                return found;
            }
            return Optional.ofNullable(listener.get(timeout.toNanos(), TimeUnit.NANOSECONDS));
        }
    }

    @Override
    public Optional<S> poll(Predicate<? super S> matcher) {
        Map<K, S> copy;
        synchronized (lock) {
            copy = ImmutableMap.copyOf(services);
        }
        return copy.values().stream()
                .filter(matcher)
                .findFirst();
    }

    @Override
    public List<S> getServices() {
        synchronized (lock) {
            return new ArrayList<>(services.values());
        }
    }

    @Override
    public void onRegistration(Predicate<? super S> matcher, Consumer<S> consumer) {
        Map<K, S> copy;
        ReactingListener<S> reactingListener = new ReactingListener<>(matcher, consumer);
        synchronized (lock) {
            copy = ImmutableMap.copyOf(services);
            listeners.add(reactingListener);
        }
        copy.values().stream()
                .forEach(reactingListener::notifyAdded);
    }
}
