/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.concurrent;

import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.GuardedBy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CopyOnWriteServiceContainer<S> implements OptionalServiceContainer<S> {

    private final Object lock = new Object();
    @GuardedBy("lock")
    private final List<S> services = new ArrayList<>();
    private final Set<Listener<S>> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
            services.add(s);
        }
        listeners.forEach(l -> l.notifyAdded(s));
    }

    @Override
    public void unregister(S s) {
        synchronized (lock) {
            services.remove(s);
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
        List<S> copy;
        synchronized (lock) {
            copy = ImmutableList.copyOf(services);
        }
        return copy.stream()
                .filter(matcher)
                .findFirst();
    }

    @Override
    public List<S> getServices() {
        synchronized (lock) {
            return ImmutableList.copyOf(services);
        }
    }

    @Override
    public void onRegistration(Predicate<? super S> matcher, Consumer<S> consumer) {
        List<S> copy;
        ReactingListener<S> reactingListener = new ReactingListener<>(matcher, consumer);
        synchronized (lock) {
            copy = ImmutableList.copyOf(services);
            listeners.add(reactingListener);
        }
        copy.stream()
                .forEach(reactingListener::notifyAdded);
    }
}
