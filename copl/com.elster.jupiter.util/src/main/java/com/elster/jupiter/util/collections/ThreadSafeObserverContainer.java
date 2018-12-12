/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ThreadSafeObserverContainer<T> implements ObserverContainer<T> {

    private Set<T> observers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public Subscription subscribe(T observer) {
        observers.add(observer);
        return () -> observers.remove(observer);
    }

    @Override
    public void notify(Consumer<T> notification) {
        observers.forEach(notification);
    }
}
